package com.mall.ware.controller;

import com.mall.common.to.WareSkuStockTO;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;
import com.mall.ware.entity.WareSkuEntity;
import com.mall.ware.service.WareSkuService;
import com.mall.ware.vo.LockStockResultVo;
import com.mall.ware.vo.WareSkuLockVo;
import com.mall.ware.vo.WareSkuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:53:14
 */
@Slf4j
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 锁定商品库存
     */
    @PostMapping("/order/stock")
    public R orderLocks(@RequestBody WareSkuLockVo wareSkuLockVo) {
        List<LockStockResultVo> lockStockResultVo = null;
        try {
            lockStockResultVo = wareSkuService.lockWares(wareSkuLockVo);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.ok().put("data", lockStockResultVo);
    }

    /**
     * 根据skuId列表查询库存剩余情况
     */
    @PostMapping("stock")
    public List<WareSkuStockTO> queryStockBySkuIds(@RequestBody List<Long> skuIds) {
        return wareSkuService.queryStockBySkuIds(skuIds);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:waresku:list")
    public R list(WareSkuVO wareSkuVO, @RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryWareSkuList(params, wareSkuVO);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    // @RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
