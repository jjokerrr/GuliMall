package com.mall.product.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.mall.common.constant.RedisConstant;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    // 空对象的存储时间
    private static final Integer NULL_TIMEOUT = 10;

    /**
     * 存储数据到redis，可避免缓存穿透问题，当timeout设置为-1的时候，为长期存储对象
     */
    public void saveBeanToRedis(String keyPrefix, Object valueObject, int timeout) {
        // 对于查询到的空对象，一并存储在redis中，避免缓存击穿问题
        if (valueObject == null) {
            saveNullObject(keyPrefix);
            return;
        }
        String jsonString = JSON.toJSONString(valueObject);
        // 通过引入随机时间来解决缓存雪崩问题
        int randomTime = RandomUtil.randomInt(0, 5);
        timeout = timeout + randomTime;

        if (timeout <= 0) {
            redisTemplate.opsForValue().set(keyPrefix, jsonString);
            return;
        }
        redisTemplate.opsForValue().set(keyPrefix, jsonString, timeout, TimeUnit.SECONDS);
    }

    /**
     * 解决缓存击穿问题
     */
    private void saveNullObject(String keyPrefix) {
        redisTemplate.opsForValue().set(keyPrefix, null, NULL_TIMEOUT, TimeUnit.MICROSECONDS);
    }

    public <T> T getBeanFromRedis(String keyPrefix, Class<T> cls) {
        String json = redisTemplate.opsForValue().get(keyPrefix);
        if (StrUtil.isBlank(json)) return null;
        return JSON.parseObject(json, cls);
    }

    /**
     * 逻辑过期解决缓存击穿问题
     */
    public <R, L> R getBeanWithMutex(String keyPrefix, L id, Class<R> type, Function<L, R> function) throws InterruptedException {
        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isEmpty(json)) {
            return null;
        }
        RedisData redisData = JSON.parseObject(json, RedisData.class);
        R res = (R) redisData.getData();
//        R res = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expire = redisData.getExpireTime();
        if (expire.isAfter(LocalDateTime.now())) {
            // 数据未过期，直接返回
            return res;
        }
        // 尝试获取互斥锁,使用redission提供的分布式锁
        String lockKey = RedisConstant.LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.tryLock(20, 30, TimeUnit.MILLISECONDS)) {
            try {
                executor.submit(() -> {
                    R byId = function.apply(id);
                    RedisData data = new RedisData();
                    data.setData(byId);
                    data.setExpireTime(LocalDateTime.now().plusSeconds(10L));
                    // 热点key不设置过期时间
                    redisTemplate.opsForValue().set(key, JSON.toJSONString(data));
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
        return res;
    }

}
