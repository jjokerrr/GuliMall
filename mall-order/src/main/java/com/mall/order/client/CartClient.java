package com.mall.order.client;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-cart")
public interface CartClient {

    /**
     * 通过用户id查询购物车信息
     */
    @GetMapping("/cart/{userId}")
    R queryCartById(@PathVariable("userId") Long userId);
}
