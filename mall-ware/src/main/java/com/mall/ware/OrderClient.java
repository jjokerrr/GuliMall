package com.mall.ware;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-order")
public interface OrderClient {
    /**
     * 根据订单号查询订单
     */
    @GetMapping("/order/order/query/{orderSn}")
    R queryOrderByOrderSn(@PathVariable("orderSn") String orderSn);
}
