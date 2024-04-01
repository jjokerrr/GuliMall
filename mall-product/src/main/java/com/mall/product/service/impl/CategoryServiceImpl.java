package com.mall.product.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.mapper.CategoryMapper;
import com.mall.product.service.CategoryService;
import com.mall.product.service.RedundantRelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Resource
    private RedundantRelation redundantRelation;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listCategoryTree() {
        // 全部类目
        List<CategoryEntity> categories = list();
        // 构建树
        List<CategoryEntity> resList = categories.stream()
                .filter(category -> category.getParentCid() == 0)
                // 添加空值处理逻辑，将空值排在最后
                .sorted(Comparator.comparing(CategoryEntity::getSort, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(category -> buildTree(category, categories))
                .collect(Collectors.toList());
        return resList;
    }

    @Override
    public void removeCategoriesByIds(List<Long> ids) {
        // TODO: 检查删除列表中的关联情况
        removeByIds(ids);
    }

    @Override
    public List<Long> getCatelogPath(Long catelogId) {
        if (catelogId.equals(0L)) return Collections.emptyList();
        List<Long> path = new ArrayList<>();
        path.add(0, catelogId);
        CategoryEntity category = query().eq("cat_id", catelogId).select("parent_cid").one();
        while (!category.getParentCid().equals(0L)) {
            path.add(0, category.getParentCid());
            category = query().eq("cat_id", category.getParentCid()).select("parent_cid").one();
        }
        return path;
    }

    @Override
    public void updateCategoryWithRelations(CategoryEntity category) {
        updateById(category);
        if (!StrUtil.isBlank(category.getName())) {
            redundantRelation.updateCategoryWithBCRelation(category);
            // TODO: 修改关联字段
        }
    }

    /**
     * 构建树形结构
     *
     * @Parameter [category：当前结点, categories：全部结点]
     * @Return CategoryEntity：构架后的对象
     */
    private CategoryEntity buildTree(CategoryEntity category, List<CategoryEntity> categories) {
        List<CategoryEntity> children = categories.stream()
                .filter(p -> p.getParentCid().equals(category.getCatId()))
                .sorted(Comparator.comparing(CategoryEntity::getSort, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        //如果一级子节点不为空，开始递归查找所有节点，直到子节点集合为空停止
        if (!CollectionUtil.isEmpty(children)) {
            for (CategoryEntity c : children) {
                buildTree(c, categories);
            }
        }
        category.setChildCategory(children);
        return category;
    }

}