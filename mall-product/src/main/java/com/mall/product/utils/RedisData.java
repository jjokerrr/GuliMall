package com.mall.product.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisData {
    /**
     * 数据
     */
    private Object data;
    /**
     * 逻辑过期时间
     */
    private LocalDateTime expireTime;
}
