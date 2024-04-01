package com.mall.product.service;

import com.mall.product.entity.BrandEntity;
import com.mall.product.entity.CategoryEntity;

public interface RedundantRelation {
    void updateBrandWithBCRelation(BrandEntity brand);

    void updateCategoryWithBCRelation(CategoryEntity category);
}
