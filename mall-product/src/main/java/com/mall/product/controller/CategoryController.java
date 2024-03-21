package com.mall.product.controller;

import com.mall.common.utils.R;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 商品三级分类
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:49
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 父子方法
     */
    @GetMapping("/list/tree")
    // @RequiresPermissions("product:category:list")
    public R listWithTree(@RequestParam Map<String, Object> params) {

        List<CategoryEntity> categoryEntityList = categoryService.listCategoryTree();
//        PageUtils page = categoryService.queryPage(params);

        return R.ok().put("data", categoryEntityList);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{catId}")
    // @RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    // @RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    // @RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category) {
        categoryService.updateById(category);
        return R.ok();
    }

    /**
     * 批量修改
     */
    @PutMapping("/update/sort")
    // @RequiresPermissions("product:category:update")
    public R updateSortBy(@RequestBody List<CategoryEntity> categories) {
        categoryService.updateBatchById(categories);

        return R.ok();
    }

    /**
     * 逻辑删除类目：
     * 以下情况不进行删除：
     * 1. 该类目非叶子结点
     * 2. 该类目与其他项有关联
     */
    @PostMapping("/delete")
    // @RequiresPermissions("product:category:delete")
    public R delete(@RequestBody List<Long> catIds) {
//        categoryService.removeByIds(Arrays.asList(catIds));
        categoryService.removeCategoriesByIds(catIds);

        return R.ok();
    }

}
