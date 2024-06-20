package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:49
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listCategoryTree();

    void removeCategoriesByIds(List<Long> ids);

    List<Long> getCatalogPath(Long catalogId);

    void updateCategoryWithRelations(CategoryEntity category);

    List<CategoryEntity> getCatalogByPLevel(int PLevel, Integer catalogShowStatus);

    Map<String, List<Catalog2Vo>> getCatalogJson();

}

