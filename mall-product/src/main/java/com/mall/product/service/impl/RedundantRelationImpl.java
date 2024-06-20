package com.mall.product.service.impl;

import com.mall.product.entity.BrandEntity;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryBrandRelationService;
import com.mall.product.service.RedundantRelation;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 更新品牌和类目之间的冗余字段
 *
 * @Parameter
 * @Return null
 */
@Service
public class RedundantRelationImpl implements RedundantRelation {
    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;


    @Override
    public void updateBrandWithBCRelation(BrandEntity brand) {
        categoryBrandRelationService.update()
                .eq("brand_id", brand.getBrandId())
                .set("brand_name", brand.getName())
                .update();
    }

    @Override
    public void updateCategoryWithBCRelation(CategoryEntity category) {
        categoryBrandRelationService.update()
                .eq("catalog_id", category.getCatId())
                .set("catalog_name", category.getName())
                .update();
    }
}
