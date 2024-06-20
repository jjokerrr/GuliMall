package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.SkuImagesEntity;
import com.mall.product.mapper.SkuImagesMapper;
import com.mall.product.service.SkuImagesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesMapper, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取sku全部的图片
     */
    @Override
    public List<SkuImagesEntity> getImgBySkuId(Long skuId) {
        if (skuId == null) return null;
        return lambdaQuery().select(SkuImagesEntity::getImgUrl)
                .select(SkuImagesEntity::getDefaultImg)
                .eq(SkuImagesEntity::getSkuId, skuId)
                .list();
    }

}