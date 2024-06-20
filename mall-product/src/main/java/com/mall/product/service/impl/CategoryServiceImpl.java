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
import com.mall.product.vo.Catalog2Vo;
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
        removeByIds(ids);
    }

    @Override
    public List<Long> getCatalogPath(Long catalogId) {
        if (catalogId.equals(0L)) return Collections.emptyList();
        List<Long> path = new ArrayList<>();
        path.add(0, catalogId);
        CategoryEntity category = query().eq("cat_id", catalogId).select("parent_cid").one();
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
        }
    }

    /**
     * 根据父节点等级和显示状态获取catalog
     */
    @Override
    public List<CategoryEntity> getCatalogByPLevel(int PLevel, Integer catalogShowStatus) {
        return query().eq("parent_cid", PLevel).eq("show_status", catalogShowStatus).list();
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        System.out.println("查询了数据库");

        //将数据库的多次查询变为一次
        List<CategoryEntity> selectList = this.list();

        //1、查出所有分类
        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //封装数据
        Map<String, List<Catalog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catalog != null) {
                        List<Catalog2Vo.Category3Vo> category3Vos = level3Catalog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catalog2Vo.Category3Vo category3Vo = new Catalog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(category3Vos);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }

            return catalog2Vos;
        }));

        return parentCid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
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