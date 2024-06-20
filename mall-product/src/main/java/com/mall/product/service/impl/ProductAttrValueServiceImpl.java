package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.ProductAttrValueEntity;
import com.mall.product.mapper.ProductAttrValueMapper;
import com.mall.product.service.ProductAttrValueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueMapper, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<ProductAttrValueEntity> queryBaseAttrforSpu(Long spuId) {
        String spuIdField = "spu_id";

        return query().eq(spuIdField, spuId).list();
    }

    @Transactional
    @Override
    public void updateAttrBySpuId(Long spuId, List<ProductAttrValueEntity> productAttrValueEntityList) {
        // 对已存在的进行修改操作，对不存在的执行新增操作
        productAttrValueEntityList.forEach(
                productAttrValueEntity -> {
                    productAttrValueEntity.setSpuId(spuId);
                    boolean update = update().eq("attr_id", productAttrValueEntity.getAttrId())
                            .eq("spu_id", spuId).update(productAttrValueEntity);
                    if (!update) {
                        this.save(productAttrValueEntity);
                    }
                });

    }

    /**
     * 更具spu查找全部的规格属性
     */
    @Override
    public List<ProductAttrValueEntity> querySpuAttrById(Long spuId) {
        if (spuId == null) return null;
        return lambdaQuery().eq(ProductAttrValueEntity::getSpuId, spuId).list();
    }

}