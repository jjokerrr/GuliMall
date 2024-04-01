package com.mall.ware.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.ware.entity.PurchaseEntity;
import com.mall.ware.vo.PurchaseDoneVO;
import com.mall.ware.vo.PurchaseMergeVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:53:14
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<PurchaseEntity> query);

    List<PurchaseEntity> queryUnreceiveList();

    void merge(PurchaseMergeVO purchaseMergeVO);

    void receivePurchaseList(List<Long> purchaseIdList);

    void finishPurchase(PurchaseDoneVO purchaseDoneVO);
}

