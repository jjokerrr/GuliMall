package com.mall.seckill.clients.fallback;

import com.mall.common.utils.R;
import com.mall.seckill.clients.CouponClient;
import org.springframework.stereotype.Component;

@Component
public class CouponClientFallBack implements CouponClient {
    @Override
    public R lastsSession(Integer days) {
        return R.error("获取订单出现异常");
    }
}
