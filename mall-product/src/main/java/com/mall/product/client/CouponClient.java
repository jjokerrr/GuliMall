package com.mall.product.client;

import com.mall.common.to.SkuReductionTO;
import com.mall.common.to.SpuBoundTO;
import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-coupon")
public interface CouponClient {

    @PostMapping("coupon/spubounds/save")
    R saveSpuBound(@RequestBody SpuBoundTO spuBoundTO);

    @PostMapping("coupon/skufullreduction/save/reduction")
    R saveSkuReduction(SkuReductionTO skuReductionTO);
}
