package com.mall.member.client;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("mall-order")
public interface OrderClient {
    /**
     * 查找用户的全部订单
     */
    @GetMapping("/order/order/list/user")
    R queryOrderByUserId(@RequestParam("userId") Long userId);

    @PostMapping("/order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);
}
