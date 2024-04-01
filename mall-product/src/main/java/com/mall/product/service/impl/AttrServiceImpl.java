package com.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.AttrAttrgroupRelationEntity;
import com.mall.product.entity.AttrEntity;
import com.mall.product.entity.AttrGroupEntity;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.mapper.AttrAttrgroupRelationMapper;
import com.mall.product.mapper.AttrGroupMapper;
import com.mall.product.mapper.AttrMapper;
import com.mall.product.service.AttrService;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.AttrRespVO;
import com.mall.product.vo.AttrVO;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrMapper, AttrEntity> implements AttrService {

    @Resource
    private AttrAttrgroupRelationMapper attrAttrgroupRelationMapper;


    @Resource
    private CategoryService categoryService;

    @Resource
    private AttrGroupMapper attrGroupMapper;

    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<AttrEntity> query) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                query
        );
        // 将类目名称和属性分组名称注入
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> pageList = (List<AttrEntity>) pageUtils.getList();
        List<AttrRespVO> attrRespVOList = pageList.stream().map((attrEntity) -> {
            AttrRespVO attrRespVO = new AttrRespVO();
            BeanUtil.copyProperties(attrEntity, attrRespVO);

            // 注入cateName字段
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            if (!BeanUtil.isEmpty(categoryEntity))
                attrRespVO.setCatelogName(categoryEntity.getName());

            // 注入attrGroupName字段
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationMapper
                    .selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
            if (!BeanUtil.isEmpty(relationEntity)) {
                // 设置组名
                AttrGroupEntity attrGroupEntity = attrGroupMapper
                        .selectById(relationEntity.getAttrGroupId());
                attrRespVO.setGroupName(attrGroupEntity.getAttrGroupName());
            }
            return attrRespVO;
        }).collect(Collectors.toList());

        pageUtils.setList(attrRespVOList);
        return pageUtils;
    }

    @Transactional
    @Override
    public void saveAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtil.copyProperties(attr, attrEntity);
        save(attrEntity);
        if (!attr.getAttrType().equals(ProductConstant.ATTR_SALE_TYPE)) {
            // 非销售属性需要保存属性组关联关系
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationMapper.insert(relationEntity);
        }

    }

    @Override
    public PageUtils queryBaseList(Long catelogId, Map<String, Object> params) {
        String catelogField = "catelog_id";
        QueryWrapper<AttrEntity> query = getQuery(catelogField, catelogId, params);
        return queryPage(params, query);
    }

    @Override
    public PageUtils querySaleList(Long catelogId, Map<String, Object> params) {
        String catelogField = "catelog_id";
        QueryWrapper<AttrEntity> query = getQuery(catelogField, catelogId, params);
        query.in("attr_type", ProductConstant.ATTR_SALE_TYPE, ProductConstant.ATTR_ALL_TYPE);
        return queryPage(params, query);
    }


    @NotNull
    private static QueryWrapper<AttrEntity> getQuery(String IdFiled, Long id, Map<String, Object> params) {
        QueryWrapper<AttrEntity> query = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StrUtil.isEmpty(key)) {
            // 精确匹配brand_id，模糊匹配name
            query.and(attrGroupIdWrapper -> {
                attrGroupIdWrapper
                        .eq("attr_id", key)
                        .or()
                        .like("attr_name", key);
            });
        }
        if (!Objects.equals(id, ProductConstant.ALL_LIST_ID)) {
            // 不为0的时候，进行id的精确查询
            query.eq(IdFiled, id);
        }
        return query;
    }

    @Override
    public AttrRespVO getAttrInfo(Long attrId) {
        AttrEntity attr = getById(attrId);
        AttrRespVO attrRespVO = new AttrRespVO();
        BeanUtil.copyProperties(attr, attrRespVO);
        // 注入分类路径
        List<Long> catelogPath = categoryService.getCatelogPath(attrRespVO.getCatelogId());
        attrRespVO.setCatelogPath(catelogPath);

        // 注入属性组信息
        AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationMapper
                .selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));
        if (!BeanUtil.isEmpty(relationEntity)) {
            attrRespVO.setAttrGroupId(relationEntity.getAttrGroupId());
        }

        return attrRespVO;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtil.copyProperties(attr, attrEntity);
        updateById(attrEntity);

        // 修改属性属性组关联关系
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(attr.getAttrGroupId());
        relationEntity.setAttrId(attr.getAttrId());
        // 修改
        int updated = attrAttrgroupRelationMapper
                .update(relationEntity, new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq(AttrAttrgroupRelationEntity::getAttrId, relationEntity.getAttrId()));
        if (updated <= 0 && !attr.getAttrType().equals(ProductConstant.ATTR_SALE_TYPE))
            // 非销售类型新增属性组关联信息
            attrAttrgroupRelationMapper
                    .insert(relationEntity);


    }


}

