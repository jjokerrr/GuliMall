package com.mall.product.client;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-seckill")
public interface SeckillClient {
    /**
     * 获取商品的秒杀预告
     */
    @GetMapping("/seckill/sku/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
