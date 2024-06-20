package com.mall.product.controller;

import com.mall.common.utils.R;
import com.mall.product.entity.CategoryBrandRelationEntity;
import com.mall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 品牌分类关联
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:49
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 品牌关联分类列表
     */
    @GetMapping("catalog/list")
    // @RequiresPermissions("product:categorybrandrelation:list")
    public R queryCatalogListFromBrand(@RequestParam("brandId") Long brandId) {
        if (brandId == null) {
            return R.ok().put("data", null);
        }
        List<CategoryBrandRelationEntity> relationList = categoryBrandRelationService.queryRelationList(brandId,"brand_id");
        return R.ok().put("data", relationList);
    }

    /**
     * 更具分类查询品牌
     */
    @GetMapping("brands/list")
    public R queryBrandListFromCatalog(@RequestParam("catId") Long catId) {
        if (catId == null) {
            return R.ok().put("data", null);
        }
        List<CategoryBrandRelationEntity> relationList = categoryBrandRelationService.queryRelationList(catId,"catalog_id");
        return R.ok().put("data", relationList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    // @RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        boolean isSucceed = categoryBrandRelationService.saveRelation(categoryBrandRelation);

        return isSucceed ? R.ok() : R.error();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody List<Long> ids) {
        categoryBrandRelationService.removeByIds(ids);

        return R.ok();
    }

}
