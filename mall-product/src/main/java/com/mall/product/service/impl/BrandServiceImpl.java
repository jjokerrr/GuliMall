package com.mall.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.BrandEntity;
import com.mall.product.mapper.BrandMapper;
import com.mall.product.service.BrandService;
import com.mall.product.service.RedundantRelation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandMapper, BrandEntity> implements BrandService {

    @Resource
    private RedundantRelation redundantRelation;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");

        QueryWrapper<BrandEntity> query = new QueryWrapper<BrandEntity>();
        if (!StrUtil.isEmpty(key)) {
            // 精确匹配brand_id，模糊匹配name
            query.and(attrGroupIdWrapper -> {
                attrGroupIdWrapper
                        .eq("brand_id", key)
                        .or()
                        .like("name", key);
            });
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void updateBrandWithRelations(BrandEntity brand) {
        updateById(brand);
        // 保证冗余字段的一致性
        if (!StrUtil.isBlank(brand.getName())) {
            redundantRelation.updateBrandWithBCRelation(brand);
            // TODO: 更新冗余关联
        }

    }

}