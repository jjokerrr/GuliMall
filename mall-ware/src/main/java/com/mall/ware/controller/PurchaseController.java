package com.mall.ware.controller;

import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;
import com.mall.ware.entity.PurchaseEntity;
import com.mall.ware.service.PurchaseService;
import com.mall.ware.vo.PurchaseDoneVO;
import com.mall.ware.vo.PurchaseMergeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 采购信息
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:53:14
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @GetMapping("/list")
    // @RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params, null);

        return R.ok().put("page", page);
    }

    /**
     * 查询全部未领取的采购单，包裹新建状态，已分配状态
     */
    @GetMapping("unreceive/list")
    public R unreceiveList() {
        List<PurchaseEntity> unreceiveList = purchaseService.queryUnreceiveList();
        PageUtils pageUtils = new PageUtils(unreceiveList);
        return R.ok().put("page", pageUtils);
    }

    /**
     * 领取采购单
     */
    @PostMapping("received")
    public R receivePurchase(@RequestBody  List<Long> purchaseIdList){
        purchaseService.receivePurchaseList(purchaseIdList);
        return R.ok();
    }

    /**
     * 完成采购需求
     */
    @PostMapping("done")
    public R comletePurchase(@RequestBody PurchaseDoneVO purchaseDoneVO){
        purchaseService.finishPurchase(purchaseDoneVO);
        return R.ok();
    }

    /**
     * 合并采购需求
     */
    @PostMapping("merge")
    public R mergePurchase(@RequestBody  PurchaseMergeVO purchaseMergeVO){
        purchaseService.merge(purchaseMergeVO);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
