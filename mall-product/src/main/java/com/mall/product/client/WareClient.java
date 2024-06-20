package com.mall.product.client;

import com.mall.common.to.WareSkuStockTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-ware")
public interface WareClient {
    /**
     * 根据skuIds列表查看库存剩余情况
     */
    @PostMapping("ware/waresku/stock")
    public List<WareSkuStockTO> queryStockBySkuIds(@RequestBody List<Long> skuIds);
}
