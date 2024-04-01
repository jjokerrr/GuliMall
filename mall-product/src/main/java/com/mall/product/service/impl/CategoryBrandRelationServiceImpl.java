package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.BrandEntity;
import com.mall.product.entity.CategoryBrandRelationEntity;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.mapper.BrandMapper;
import com.mall.product.mapper.CategoryBrandRelationMapper;
import com.mall.product.mapper.CategoryMapper;
import com.mall.product.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationMapper, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryMapper categoryMapper;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryBrandRelationEntity> queryRelationList(Long brandId, String idType) {
        return query()
                .eq(idType, brandId)
                .list();
    }

    @Override
    @Transactional
    public boolean saveRelation(CategoryBrandRelationEntity categoryBrandRelation) {
        CategoryEntity category = categoryMapper.selectById(categoryBrandRelation.getCatelogId());
        BrandEntity brand = brandMapper.selectById(categoryBrandRelation.getBrandId());
        categoryBrandRelation.setCatelogName(category.getName());
        categoryBrandRelation.setBrandName(brand.getName());
        return save(categoryBrandRelation);
    }


}