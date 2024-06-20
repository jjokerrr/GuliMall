package com.mall.product.client;

import com.mall.common.es.SkuEsModel;
import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-search")
public interface SearchClient {
    @PostMapping("search/save/product")
    R saveProductStatusUp(@RequestBody List<SkuEsModel> skuEsModelList);
}
