package com.mall.product.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.AttrAttrgroupRelationEntity;
import com.mall.product.entity.AttrEntity;
import com.mall.product.mapper.AttrAttrgroupRelationMapper;
import com.mall.product.service.AttrAttrgroupRelationService;
import com.mall.product.service.AttrService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationMapper, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public boolean removeBatchRelation(List<AttrAttrgroupRelationEntity> relationEntityList) {

        return baseMapper.deleteBatchRelations(relationEntityList);
    }

    @Override
    public List<AttrEntity> queryAttrByAttrGroupId(Long attrGroupId) {
        List<Long> attrIds = query()
                .eq("attr_group_id", attrGroupId)
                .list()
                .stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(attrIds)) {
            return Collections.emptyList();
        }
        return attrService.listByIds(attrIds);
    }


}