package com.mall.seckill.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.mall.common.constant.OrderMqConstant;
import com.mall.common.constant.RedisConstant;
import com.mall.common.to.mq.SeckillOrderTo;
import com.mall.common.utils.UserHolder;
import com.mall.seckill.service.SeckillService;
import com.mall.seckill.to.SeckillSkuRedisTo;
import com.mall.seckill.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 获取在秒杀时间内的商品列表
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillInfo() {
        long now = new Date().getTime();
        List<SeckillSkuRedisTo> skuRedisTos = new ArrayList<>();
        Set<String> sessionKeys = redisUtil.scanKeys("seckill:session:*_*");
        for (String sessionKey : sessionKeys) {
            String[] keySplit = sessionKey.split(":");
            String sessionId = keySplit[3];
            String sessionTimeKey = keySplit[2];
            String[] times = sessionTimeKey.split("_");
            if (Long.parseLong(times[0]) <= now && Long.parseLong(times[1]) >= now) {
                Set<String> members = stringRedisTemplate.opsForSet().members(sessionKey);
                if (CollectionUtil.isEmpty(members)) continue;
                // 添加秒杀key
                for (String member : members) {
                    String key = RedisConstant.SECKILL_SESSION_SKU_PREFIX + sessionId + ":" + member;
                    SeckillSkuRedisTo value = redisUtil.getValue(key, SeckillSkuRedisTo.class);
                    skuRedisTos.add(value);
                }
            }
        }
        return skuRedisTos;
    }

    /**
     * 获取最近的sku秒杀信息
     */
    @Override
    public SeckillSkuRedisTo getLastSkuSeckillInfo(Long skuId) {
        // 扫描全部关于当前sku的秒杀场次信息
        Set<String> sessionSkuKeys = redisUtil.scanKeys(RedisConstant.SECKILL_SESSION_SKU_PREFIX + "*:" + skuId);
        List<String> session = new ArrayList<>();
        for (String skuKeys : sessionSkuKeys) {
            String sessionId = skuKeys.split(":")[3];
            Set<String> sessionKeys = redisUtil.scanKeys(RedisConstant.SECKILL_SESSION_PREFIX + "*:" + sessionId);
            session.addAll(sessionKeys);
        }
        if (CollectionUtil.isEmpty(session)) {
            return null;
        }
        Collections.sort(session);
        // 最近场次的session
        String sessionKey = session.get(0);
        String lastSessionId = sessionKey.split(":")[3];
        SeckillSkuRedisTo seckillSkuRedisTo = redisUtil.getValue(RedisConstant.SECKILL_SESSION_SKU_PREFIX + lastSessionId + ":" + skuId, SeckillSkuRedisTo.class);
        // 脱敏处理
        if (seckillSkuRedisTo.getStartTime() > new Date().getTime()) {
            seckillSkuRedisTo.setRandomCode(null);
        }
        return seckillSkuRedisTo;
    }

    @Override
    public String seckillKill(Long sessionId, Long skuId, String randomCode, Integer num) {

        SeckillSkuRedisTo skuRedisTo = redisUtil.getValue(RedisConstant.SECKILL_SESSION_SKU_PREFIX + sessionId + ":" + skuId, SeckillSkuRedisTo.class);
        // 合法性校验
        if (skuRedisTo == null) {
            log.info("秒杀商品不存在");
            return null;
        }
        Long startTime = skuRedisTo.getStartTime();
        Long endTime = skuRedisTo.getEndTime();
        long time = new Date().getTime();
        if (time < startTime || time > endTime) return null;
        // 随机码验证
        if (!skuRedisTo.getRandomCode().equals(randomCode)) {
            log.info("随机码验证失败");
            return null;
        }
        // 验证限购数量
        Integer seckillLimit = skuRedisTo.getSeckillLimit();
        Integer userPurchasedQuantity = getUserPurchasedQuantity(sessionId, skuId);
        if (seckillLimit - userPurchasedQuantity < num) {
            log.error("用户达到限购数量");
            return null;
        }
        // 进行下单逻辑
        // 获取信号量
        if (trySemaphore(randomCode, num)) return null;
        // 添加用户限购数量
        addUserPurchasedQuantity(sessionId, skuId, num);

        String orderSn = String.valueOf(redisUtil.generateId("order"));

        // 载入mq进行流量削峰
        SeckillOrderTo seckillOrderTo = buildOrderTo(skuRedisTo, orderSn, num);
        rabbitTemplate.convertAndSend(OrderMqConstant.ORDER_EVENT_EXCHANGE, OrderMqConstant.ORDER_CREATE_SECKILL_ORDER_ROUTING_KEY, seckillOrderTo);
        // 下单
        return orderSn;


    }

    private SeckillOrderTo buildOrderTo(SeckillSkuRedisTo skuRedisTo, String orderSn, Integer num) {
        SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
        seckillOrderTo.setOrderSn(orderSn);
        seckillOrderTo.setSeckillPrice(skuRedisTo.getSeckillPrice());
        seckillOrderTo.setNum(num);
        seckillOrderTo.setMemberId(UserHolder.getUser().getId());
        seckillOrderTo.setSkuId(skuRedisTo.getSkuId());
        seckillOrderTo.setPromotionSessionId(skuRedisTo.getPromotionSessionId());
        return seckillOrderTo;
    }

    private boolean trySemaphore(String randomCode, Integer num) {
        RSemaphore semaphore = redissonClient.getSemaphore(RedisConstant.SKU_SEMAPHORE_PREFIX + randomCode);
        try {
            if (!semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS)) {
                // 库存数量不足
                return true;
            }
        } catch (InterruptedException e) {
            return true;
        }
        return false;
    }

    private void addUserPurchasedQuantity(Long sessionId, Long skuId, Integer nums) {
        Long userId = UserHolder.getUser().getId();
        stringRedisTemplate.opsForValue().increment(RedisConstant.SECKILL_SESSION_SKU_PREFIX + sessionId + ":" + skuId + ":" + userId, nums);

    }

    private Integer getUserPurchasedQuantity(Long sessionId, Long skuId) {
        Long userId = UserHolder.getUser().getId();
        String key = RedisConstant.SECKILL_SESSION_SKU_PREFIX + sessionId + ":" + skuId + ":" + userId;
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(key))) {
            return 0;
        }
        return redisUtil.getValue(key, Integer.class);
    }
}
