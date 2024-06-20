package com.mall.search.service;

import com.mall.common.es.SkuEsModel;

import java.util.List;

public interface ProductSaveService {
    Boolean saveProduct(List<SkuEsModel> skuEsModelList);
}
