package com.mall.product.controller;

import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;
import com.mall.product.entity.AttrAttrgroupRelationEntity;
import com.mall.product.entity.AttrEntity;
import com.mall.product.entity.AttrGroupEntity;
import com.mall.product.service.AttrAttrgroupRelationService;
import com.mall.product.service.AttrGroupService;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.AttrGroupRelationVO;
import com.mall.product.vo.AttrGroupWithAttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:49
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        // 拼接根路径，用于前端显示
        Long catalogId = attrGroup.getCatalogId();
        List<Long> catalogPath = categoryService.getCatalogPath(catalogId);
        attrGroup.setCatalogPath(catalogPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 根据商品类目查找商品属性组
     *
     * @Parameter [categoryId, params]
     * @Return R
     */
    @GetMapping("/list/{categoryId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R getAttrByCategoryId(@PathVariable(value = "categoryId") Long categoryId, @RequestParam Map<String, Object> params) {
        PageUtils page = attrGroupService.getPageByCategoryId(params, categoryId);
        return R.ok().put("page", page);
    }


    /**
     * 查询属性组全部关联属性
     */
    @GetMapping("{attrGroupId}/attr/relation")
    public R getRelationAttr(@PathVariable("attrGroupId") Long attrGroupId) {
        List<AttrEntity> attrEntityList = attrGroupService.getAllAttr(attrGroupId);
        return R.ok().put("data", attrEntityList);
    }


    /**
     * 移除关联属性
     */
    @PostMapping("attr/relation/delete")
    public R removeRelationAttr(@RequestBody List<AttrGroupRelationVO> relationList) {
        attrGroupService.removeRelationAttr(relationList);
        return R.ok();
    }

    /**
     * 查询本分类下全部未关联属性
     */
    @GetMapping("{attrGroupId}/noattr/relation")
    public R getNoRelationAttr(@PathVariable("attrGroupId") Long attrGroupId, @RequestParam Map<String, Object> params) {
        PageUtils page = attrGroupService.getNoRelationAttr(attrGroupId, params);
        return R.ok().put("page", page);

    }

    /**
     * 新增属性组和属性之间的关联关系
     */
    @PostMapping("attr/relation")
    public R saveAttrAttrRelation(@RequestBody List<AttrAttrgroupRelationEntity> relationEntityList) {
        attrAttrgroupRelationService.saveBatch(relationEntityList);
        return R.ok();

    }

    /**
     * 获取属性组及其关联属性
     */
    @GetMapping("{catalogId}/withattr")
    public R queryAttrGroupWithAttr(@PathVariable("catalogId") Long catalogId) {
        List<AttrGroupWithAttrVO> attrGroupWithAttrVOList = attrGroupService.queryAttrGroupWithAttrByCatalogId(catalogId);
        return R.ok().put("data", attrGroupWithAttrVOList);
    }

}
