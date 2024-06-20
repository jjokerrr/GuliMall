package com.mall.order.client;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-member")
public interface MemberClient {

    /**
     * 通过用户id获取地址列表
     */
    @GetMapping("member/memberreceiveaddress/list/{userId}")
    R queryListByUserId(@PathVariable("userId") Long userId);
}
