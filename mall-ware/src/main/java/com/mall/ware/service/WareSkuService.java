package com.mall.ware.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.to.WareSkuStockTO;
import com.mall.common.utils.PageUtils;
import com.mall.ware.entity.WareSkuEntity;
import com.mall.ware.vo.LockStockResultVo;
import com.mall.ware.vo.WareSkuLockVo;
import com.mall.ware.vo.WareSkuVO;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:53:14
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<WareSkuEntity> query);

    PageUtils queryWareSkuList(Map<String, Object> params, WareSkuVO wareSkuVO);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<WareSkuStockTO> queryStockBySkuIds(List<Long> skuIds);

    List<LockStockResultVo> lockWares(WareSkuLockVo wareSkuLockVo);

}

