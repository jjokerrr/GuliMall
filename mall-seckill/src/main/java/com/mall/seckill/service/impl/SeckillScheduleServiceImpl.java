package com.mall.seckill.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mall.common.constant.RedisConstant;
import com.mall.common.utils.R;
import com.mall.seckill.clients.CouponClient;
import com.mall.seckill.clients.ProductClient;
import com.mall.seckill.service.SeckillScheduleService;
import com.mall.seckill.to.SeckillSkuRedisTo;
import com.mall.seckill.vo.SeckillSessionWithSkusVo;
import com.mall.seckill.vo.SeckillSkuVo;
import com.mall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillScheduleServiceImpl implements SeckillScheduleService {
    @Resource
    private CouponClient couponClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ProductClient productClient;
    @Resource
    private RedissonClient redissonClient;


    /**
     * 上架最近三天的秒杀商品
     */
    @Override
    public void uploadSeckillSku3LastDays() {
        R couponResponse = couponClient.lastsSession(3);
        if (!couponResponse.getCode().equals("0")) {
            log.error("coupon服务远程调配失败");
            throw new RuntimeException("coupon服务远程调配失败");
        }
        List<SeckillSessionWithSkusVo> seckillSessionWithSkusVos = couponResponse.getData("data", new TypeReference<List<SeckillSessionWithSkusVo>>() {
        });
        // 上架活动以及上架商品
        for (SeckillSessionWithSkusVo seckillSessionWithSkusVo : seckillSessionWithSkusVos) {
            saveEventToRedis(seckillSessionWithSkusVo);
            saveSkuListToRedis(new HashSet<>(seckillSessionWithSkusVo.getRelationSkus()), seckillSessionWithSkusVo.getStartTime(), seckillSessionWithSkusVo.getEndTime());
        }
    }

    /**
     * 保存秒杀sku详细信息，对于数据对象，采用了随机吗进行存储。对于秒杀商品的更新操作
     * ，如果秒杀商品需要更新，需要更新操作将其对应的redis数据和信号量进行删除操作
     * ，之后等待定时任务重新将其上架。因此，秒杀操作需要至少提前一天进行操作
     * 随机码的作用：
     * 1. 在秒杀开始的时候随机码生效，防止预制脚本请求
     * 2. 为秒杀商品信号量添加随机码，防止恶意攻击信号量
     */
    private void saveSkuListToRedis(Set<SeckillSkuVo> relationSkus, Date startTime, Date endTime) {
        if (CollectionUtil.isEmpty(relationSkus))
            return;
        for (SeckillSkuVo skuVo : relationSkus) {
            // 创建rediskey
            String key = RedisConstant.SECKILL_SESSION_SKU_PREFIX + skuVo.getPromotionSessionId() + ":" + skuVo.getSkuId();
            // 考虑幂等型问题
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
                return;
            }
            String token = UUID.randomUUID().toString().replace("-", "");
            // 获取sku基本信息
            R skuInfoResponse = productClient.info(skuVo.getSkuId());
            if (!skuInfoResponse.getCode().equals("0")) {
                throw new RuntimeException("请求sku信息失败");
            }
            SkuInfoVo skuInfo = skuInfoResponse.getData("skuInfo", new TypeReference<SkuInfoVo>() {
            });
            SeckillSkuRedisTo redisTo = buildSkuRedisTo(startTime, endTime, skuVo, skuInfo, token);

            //序列化json格式存入Redis中
            String seckillValue = JSON.toJSONString(redisTo);
            long expireDays = getExpireDays(endTime);
            System.out.println(expireDays);
            stringRedisTemplate.opsForValue().set(key, seckillValue, expireDays, TimeUnit.DAYS);


            //5、使用库存作为分布式Redisson信号量（限流）
            // 使用库存作为分布式信号量
            RSemaphore semaphore = redissonClient.getSemaphore(RedisConstant.SKU_SEMAPHORE_PREFIX + skuVo.getPromotionSessionId() + ":" + skuVo.getSkuId());
            // 商品可以秒杀的数量作为信号量
            semaphore.trySetPermits(skuVo.getSeckillCount());

        }
    }

    private static SeckillSkuRedisTo buildSkuRedisTo(Date startTime, Date endTime, SeckillSkuVo skuVo, SkuInfoVo skuInfo, String token) {
        // 封装redis存储对象
        SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
        redisTo.setSkuInfo(skuInfo);
        //2、sku的秒杀信息
        BeanUtils.copyProperties(skuVo, redisTo);

        //3、设置当前商品的秒杀时间信息
        redisTo.setStartTime(startTime.getTime());
        redisTo.setEndTime(endTime.getTime());

        //4、设置商品的随机码（防止恶意攻击）
        redisTo.setRandomCode(token);
        return redisTo;
    }

    private long getExpireDays(Date endTime) {
        // 将 Date 对象转换为 LocalDate
        LocalDate dateToCompare = endTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // 计算两个日期之间相差的天数

        return Math.abs(ChronoUnit.DAYS.between(dateToCompare, LocalDate.now())) + 1L;
    }

    /**
     * 上架秒杀场次信息
     */
    private void saveEventToRedis(SeckillSessionWithSkusVo seckillSessionWithSkusVo) {
        // 秒杀场次信息，key 秒杀起始时间，秒杀结束时间
        long startTime = seckillSessionWithSkusVo.getStartTime().getTime();
        long endTime = seckillSessionWithSkusVo.getEndTime().getTime();

        String key = RedisConstant.SECKILL_SESSION_PREFIX + startTime + "_" + endTime + ":" + seckillSessionWithSkusVo.getId();
        // 每次重新添加是，删除旧的数据
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(hasKey)) stringRedisTemplate.delete(key);
        // 保存场次信息
        List<String> skuIds = seckillSessionWithSkusVo.getRelationSkus().stream().map(((seckillSkuVo) -> String.valueOf(seckillSkuVo.getSkuId()))).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(skuIds)) {
            return;
        }
        for (String skuId : skuIds) {
            stringRedisTemplate.opsForSet().add(key, skuId);
        }
    }
}
