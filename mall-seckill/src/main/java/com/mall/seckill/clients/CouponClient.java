package com.mall.seckill.clients;

import com.mall.common.utils.R;
import com.mall.seckill.clients.fallback.CouponClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "mall-coupon",fallback = CouponClientFallBack.class)
public interface CouponClient {
    /**
     * 查询最近上架的秒杀活动
     */
    @GetMapping("/coupon/seckillsession/lastsSession/{days}")
    R lastsSession(@PathVariable("days") Integer days);
}
