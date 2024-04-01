package com.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.mapper.SkuInfoMapper;
import com.mall.product.service.SkuInfoService;
import com.mall.product.vo.SkuListVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<SkuInfoEntity> query) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils querySkuList(Map<String, Object> params, SkuListVO skuListVO) {
        String key = (String) params.get("key");
        QueryWrapper<SkuInfoEntity> query = new QueryWrapper<>();
        String skuIdField = "sku_id";
        String skuNameFiled = "sku_name";
        // 构造key query
        buildKeyQuery(key, query, skuIdField, skuNameFiled);

        buildSkuQuery(skuListVO, query);

        return queryPage(params, query);
    }


    private void buildSkuQuery(SkuListVO spuListVO, QueryWrapper<SkuInfoEntity> query) {
        if (BeanUtil.isEmpty(spuListVO)) {
            return;
        }
        String catelogIdField = "catelog_id";
        String brandIdField = "brand_id";
        String priceField = "price";

        Long brandId = spuListVO.getBrandId();
        String max = spuListVO.getMax();
        String min = spuListVO.getMin();
        Long catelogId = spuListVO.getCatelogId();


        // 构造catelogId的查询
        buildQuery(catelogId, query, catelogIdField);
        // brandId
        buildQuery(brandId, query, brandIdField);
        // price

        if (!StrUtil.isBlank(max) && new BigDecimal(max).compareTo(BigDecimal.ZERO) > 0) {
            query.le(priceField, max);
        }
        if (!StrUtil.isBlank(min) && new BigDecimal(min).compareTo(BigDecimal.ZERO) > 0) {
            query.ge(priceField, min);
        }
    }

    private void buildQuery(Number key, QueryWrapper<SkuInfoEntity> query, String keyField) {
        if (!(key == null) && !key.toString().equals(ProductConstant.ALL_LIST_ID.toString())) {
            query.eq(keyField, key);
        }
    }

    private void buildKeyQuery(String key, QueryWrapper<SkuInfoEntity> query, String skuIdField, String skuNameFiled) {
        if (!StrUtil.isBlank(key)) {
            query.and(queryWrapper -> queryWrapper
                    .eq(skuIdField, key)
                    .or()
                    .like(skuNameFiled, key));
        }
    }


}