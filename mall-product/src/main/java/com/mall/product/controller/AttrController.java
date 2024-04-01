package com.mall.product.controller;

import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;
import com.mall.product.entity.ProductAttrValueEntity;
import com.mall.product.service.AttrService;
import com.mall.product.service.ProductAttrValueService;
import com.mall.product.vo.AttrRespVO;
import com.mall.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品属性
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:48
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 查询属性基础列表
     */
    @GetMapping("base/list/{id}")
    // @RequiresPermissions("product:attr:list")
    public R queryBaseList(@PathVariable("id") Long catelogId, @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryBaseList(catelogId, params);

        return R.ok().put("page", page);
    }

    /**
     * 查询spu对应的全部属性
     */
    @GetMapping("base/listforspu/{spuId}")
    // @RequiresPermissions("product:attr:list")
    public R queryBaseAttrofSpu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> productAttrValueEntityList = productAttrValueService.queryBaseAttrforSpu(spuId);

        return R.ok().put("data", productAttrValueEntityList);
    }

    /**
     * 查询属性销售列表
     */
    @GetMapping("sale/list/{id}")
    // @RequiresPermissions("product:attr:list")
    public R querySaleList(@PathVariable("id") Long catelogId, @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.querySaleList(catelogId, params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrRespVO attr = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVO attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改商品规格
     */
    @PutMapping("update/{spuId}")
    public R updateAttrById(@PathVariable("spuId") Long spuId,@RequestBody List<ProductAttrValueEntity> productAttrValueEntityList){
        productAttrValueService.updateAttrBySpuId(spuId,productAttrValueEntityList);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVO attr) {
        attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
