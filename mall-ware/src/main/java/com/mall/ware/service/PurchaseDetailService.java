package com.mall.ware.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.ware.entity.PurchaseDetailEntity;
import com.mall.ware.vo.PurchaseDetailListVO;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:53:14
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<PurchaseDetailEntity> query);

    PageUtils queryPurchaseDetailList(PurchaseDetailListVO purchaseDetailListVO, Map<String, Object> params);

    List<PurchaseDetailEntity> queryPurchaseDetailListByPurchaseId(Long purchaseId);
}

