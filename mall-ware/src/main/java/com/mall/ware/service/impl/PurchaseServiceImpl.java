package com.mall.ware.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.WareConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.ware.entity.PurchaseDetailEntity;
import com.mall.ware.entity.PurchaseEntity;
import com.mall.ware.mapper.PurchaseMapper;
import com.mall.ware.service.PurchaseDetailService;
import com.mall.ware.service.PurchaseService;
import com.mall.ware.service.WareSkuService;
import com.mall.ware.vo.PurchaseDoneVO;
import com.mall.ware.vo.PurchaseItemVO;
import com.mall.ware.vo.PurchaseMergeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseMapper, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;

    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<PurchaseEntity> query) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    /**
     * 查询未领取的采购单
     */
    @Override
    public List<PurchaseEntity> queryUnreceiveList() {
        // 这里不要写in，会导致索引失效
//        return this.query().in("status", WareConstant.PURCHASE_NEW_STATUS, WareConstant.PURCHASE_ASSIGNED_STATUS).list();
        return this.query()
                .eq("status", WareConstant.PURCHASE_NEW_STATUS)
                .or()
                .eq("status", WareConstant.PURCHASE_ASSIGNED_STATUS)
                .list();
    }

    /**
     * 合并采购需求到采购单。若不选择采购单则创建新的采购单
     */
    @Transactional
    @Override
    public void merge(PurchaseMergeVO purchaseMergeVO) {
        Long purchaseId = purchaseMergeVO.getPurchaseId();
        List<Long> items = purchaseMergeVO.getItems();
        if (CollectionUtil.isEmpty(items)) {
            // 无采购需求，合并无意义
            return;
        }
        if (purchaseId == null) {
            // 未填写采购单，创建新的采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PURCHASE_NEW_STATUS);
            save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        // 合并采购单
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntityList = items.stream().map(id -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PURCHASE_DETAIL_ASSIGNED_STATUS);
            purchaseDetailEntity.setId(id);
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(purchaseDetailEntityList);


    }

    /**
     * 领取采购单
     */
    @Transactional
    @Override
    public void receivePurchaseList(List<Long> purchaseIdList) {
        // 检索当前用户所有的采购单,由于暂时没有用户系统，直接根据采购单id过滤
        List<PurchaseEntity> purchaseEntityList = purchaseIdList.stream().map(this::getById)
                .filter(purchaseEntity
                        -> Objects.equals(purchaseEntity.getStatus(), WareConstant.PURCHASE_NEW_STATUS)
                        || Objects.equals(purchaseEntity.getStatus(), WareConstant.PURCHASE_ASSIGNED_STATUS))
                .peek(purchaseEntity -> purchaseEntity.setStatus(WareConstant.PURCHASE_RECEIVED_STATUS))
                .collect(Collectors.toList());
        // 保存
        this.updateBatchById(purchaseEntityList);

        // 修改采购单关联的商品状态

        purchaseEntityList.forEach(item -> {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService
                    .queryPurchaseDetailListByPurchaseId(item.getId());
            List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailEntities
                    .stream()
                    .peek(purchaseDetailEntity -> purchaseDetailEntity
                            .setStatus(WareConstant.PURCHASE_DETAIL_RECEIVED_STATUS))
                    .collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntityList);
        });
    }

    @Transactional
    @Override
    public void finishPurchase(PurchaseDoneVO purchaseDoneVO) {
        Long id = purchaseDoneVO.getId();


        //改变采购项的状态
        boolean flag = true;
        List<PurchaseItemVO> items = purchaseDoneVO.getItems();

        List<PurchaseDetailEntity> updateItemList = new ArrayList<>();
        for (PurchaseItemVO item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (Objects.equals(item.getStatus(), WareConstant.PURCHASE_DETAIL_ERR_STATUS)) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PURCHASE_DETAIL_COMPLETED_STATUS);
                ////3、将成功采购的进行入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());

            }
            detailEntity.setId(item.getItemId());
            updateItemList.add(detailEntity);
        }

        purchaseDetailService.updateBatchById(updateItemList);

        //1、改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PURCHASE_COMPLETED_STATUS : WareConstant.PURCHASE_ERR_STATUS);
        this.updateById(purchaseEntity);

    }

}