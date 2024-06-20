package com.mall.cart.client;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-product")
public interface ProductClient {

    /**
     * 获取sku信息
     */
    @GetMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);


    /**
     * 查询sku全部销售组合信息
     */
    @GetMapping("product/skuinfo/saleAttr/{skuId}")
    R getSaleAttrsBySkuId(@PathVariable("skuId") Long skuId);

    @PostMapping("product/skuinfo/getCurrentPrice")
    R getCurrentPrice(@RequestBody List<Long> skuIds);

}
