package com.mall.product.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.mapper.SpuImagesMapper;
import com.mall.product.entity.SpuImagesEntity;
import com.mall.product.service.SpuImagesService;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesMapper, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

}