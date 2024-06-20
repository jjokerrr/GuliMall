package com.mall.seckill.clients.fallback;

import com.mall.common.utils.R;
import com.mall.seckill.clients.ProductClient;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallBack implements ProductClient {
    @Override
    public R info(Long skuId) {
        return R.error("获取商品信息出现异常");
    }
}
