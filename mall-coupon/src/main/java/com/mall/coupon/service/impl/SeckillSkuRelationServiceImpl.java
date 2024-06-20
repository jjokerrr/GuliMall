package com.mall.coupon.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.coupon.entity.SeckillSkuRelationEntity;
import com.mall.coupon.mapper.SeckillSkuRelationMapper;
import com.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationMapper, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<SeckillSkuRelationEntity> query) {
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils listByQuery(Map<String, Object> params) {
        String promotionSessionIdFiled = "promotion_session_id";
        String keyField = "key";
        Long promotionId = (Long) params.get(promotionSessionIdFiled);
        String key = (String) params.get(keyField);
        QueryWrapper<SeckillSkuRelationEntity> query = new QueryWrapper<>();
        buildQuery(promotionId, query, promotionSessionIdFiled);
        buildKeyQuery(key, query, "id", "sku_id");
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

}