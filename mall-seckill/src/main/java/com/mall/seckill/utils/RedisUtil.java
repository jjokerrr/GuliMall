package com.mall.seckill.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 */
@Component
public class RedisUtil {
    final long BEGIN_TIMESTAMP = 946684800L;      // 2000-01-01-00:00:00时间
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 将对象序列化成json存储在redis中
     */
    public <T> void setValue(String key, T value, Long timeout, TimeUnit timeUnit) {
        String jsonString = JSON.toJSONString(value);
        stringRedisTemplate.opsForValue().set(key, jsonString, timeout, timeUnit);
    }

    /**
     * 从redis中获取对象
     */
    public <T> T getValue(String key, Class<T> cls) {
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(jsonStr)) {
            return null;
        }
        return JSON.parseObject(jsonStr, cls);
    }

    /**
     * 获取符合条件的keys
     */
    public Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        try (Cursor<byte[]> cursor = stringRedisTemplate.executeWithStickyConnection(
                redisConnection -> redisConnection.scan(options))) {

            if (cursor != null) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            }
        }
        return keys;
    }

    public long generateId(String prefix) {
        // 生成时间戳
        LocalDateTime currentTime = LocalDateTime.now();
        long timeStamp = currentTime.toEpochSecond(ZoneOffset.UTC) - BEGIN_TIMESTAMP;

        // 生成当前序列号
        // 获取当前月份
        String currentMonth = currentTime.format(DateTimeFormatter.ofPattern("yyyy:MM"));
        String key = "icr:" + prefix + ":" + currentMonth;
        Long increment = stringRedisTemplate.opsForValue().increment(key);
        increment = increment != null ? increment : 0L;
        // 生成id
        return (timeStamp << 32) | increment;
    }


}
