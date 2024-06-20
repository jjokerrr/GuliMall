package com.mall.seckill.clients;

import com.mall.common.utils.R;
import com.mall.seckill.clients.fallback.ProductClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "mall-product", fallback = ProductClientFallBack.class)
public interface ProductClient {
    /**
     * 信息
     */
    @GetMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
