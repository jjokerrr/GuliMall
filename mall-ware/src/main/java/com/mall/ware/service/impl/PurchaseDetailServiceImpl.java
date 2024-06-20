package com.mall.ware.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.ware.entity.PurchaseDetailEntity;
import com.mall.ware.mapper.PurchaseDetailMapper;
import com.mall.ware.service.PurchaseDetailService;
import com.mall.ware.vo.PurchaseDetailListVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailMapper, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<PurchaseDetailEntity> query) {
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPurchaseDetailList(PurchaseDetailListVO purchaseDetailListVO, Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> query = new QueryWrapper<>();
        String key = (String) params.get("key");
        String purchaseIdField = "purchase_id";
        String skuIdField = "sku_id";
        String wareIdField = "ware_id";
        String statusField = "status";
        Integer status = purchaseDetailListVO.getStatus();
        Long wareId = purchaseDetailListVO.getWareId();
        buildKeyQuery(key, query, purchaseIdField, skuIdField);
        buildQuery(wareId, query, wareIdField);
        if (!(status == null)) {
            query.eq(statusField, status);
        }

        return queryPage(params, query);
    }

    private <T> void buildQuery(Number key, QueryWrapper<T> query, String keyField) {
        if (!(key == null) && !key.toString().equals(ProductConstant.ALL_LIST_ID.toString())) {
            query.eq(keyField, key);
        }
    }

    private <T> void buildKeyQuery(String key, QueryWrapper<T> query, String idField, String nameField) {
        if (!StrUtil.isBlank(key)) {
            query.and(queryWrapper -> queryWrapper.eq(idField, key)
                    .or()
                    .like(nameField, key));
        }
    }

    /**
     * 根据采购单id获取对应的采购商品列表
     */
    public List<PurchaseDetailEntity> queryPurchaseDetailListByPurchaseId(Long purchaseId) {
        return query().eq("purchase_id", purchaseId).list();
    }
}