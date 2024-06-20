package com.mall.seckill.service;

import com.mall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    List<SeckillSkuRedisTo> getCurrentSeckillInfo();

    SeckillSkuRedisTo getLastSkuSeckillInfo(Long skuId);

    String seckillKill(Long sessionId, Long skuId, String randomCode, Integer num);
}
