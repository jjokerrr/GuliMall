package com.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.AttrAttrgroupRelationEntity;
import com.mall.product.entity.AttrEntity;
import com.mall.product.entity.AttrGroupEntity;
import com.mall.product.mapper.AttrGroupMapper;
import com.mall.product.service.AttrAttrgroupRelationService;
import com.mall.product.service.AttrGroupService;
import com.mall.product.service.AttrService;
import com.mall.product.vo.AttrGroupRelationVO;
import com.mall.product.vo.AttrGroupWithAttrVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Resource
    private AttrService attrService;


    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<AttrGroupEntity> query) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils getPageByCategoryId(Map<String, Object> params, Long categoryId) {

        QueryWrapper<AttrGroupEntity> query = new QueryWrapper<AttrGroupEntity>();
        if (!Objects.equals(categoryId, ProductConstant.ALL_LIST_ID)) {
            // 不为0的时候，进行id的精确查询
            query.eq("catelog_id", categoryId);
        }
        // 参数不为零，根据查询条件获取列表
        String preciseFiled = "attr_group_id";
        String fuzzyField = "attr_group_name";
        buildKeyQuery(params, query, preciseFiled, fuzzyField);

        return queryPage(params, query);
    }

    private <T> void buildKeyQuery(Map<String, Object> params, QueryWrapper<T> query, String preciseFiled, String fuzzyField) {
        String key = (String) params.get("key");

        if (!StrUtil.isBlank(key)) {
            // 精确匹配attr_group_id，模糊匹配attr_group_name
            query.and(queryWrapper -> queryWrapper
                    .eq(preciseFiled, key)
                    .or()
                    .like(fuzzyField, key));
        }
    }

    @Override
    public List<AttrEntity> getAllAttr(Long attrGroupId) {
        // 获取全部关联id
        List<AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationService
                .query()
                .eq("attr_group_id", attrGroupId)
                .list();
        if (CollectionUtil.isEmpty(relationEntityList)) {
            return null;
        }
        // 查询全部属性
        List<Long> attrIds = relationEntityList
                .stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        return attrService.listByIds(attrIds);

    }

    @Override
    public PageUtils getNoRelationAttr(Long attrGroupId, Map<String, Object> params) {
        // 查询当前属性组的类目信息
        AttrGroupEntity attrGroup = getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();

        // 查询当前属性组全部关联的属性
        List<Long> relationAttrIdList = attrAttrgroupRelationService
                .query()
                .eq("attr_group_id", attrGroupId)
                .list()
                .stream()
                .map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        // 查询满足条件的属性
        QueryWrapper<AttrEntity> query = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId)
                .notIn(!CollectionUtil.isEmpty(relationAttrIdList), "attr_id", relationAttrIdList);

        buildKeyQuery(params, query, "attr_id", "attr_name");


        return attrService.queryPage(params, query);

    }

    @Override
    public List<AttrGroupWithAttrVO> queryAttrGroupWithAttrByCatelogId(Long catelogId) {
        // 查询全部属性组
        List<AttrGroupEntity> attrGroupEntityList = query().eq("catelog_id", catelogId).list();
        // 查询属性组下的全部属性
        return attrGroupEntityList.stream().map(attrGroupEntity -> {
            List<AttrEntity> attrEntityList = attrAttrgroupRelationService.queryAttrByAttrGroupId(attrGroupEntity.getAttrGroupId());
            AttrGroupWithAttrVO attrGroupWithAttrVO = new AttrGroupWithAttrVO();
            BeanUtil.copyProperties(attrGroupEntity, attrGroupWithAttrVO);
            attrGroupWithAttrVO.setAttrs(attrEntityList);
            return attrGroupWithAttrVO;
        }).collect(Collectors.toList());
    }

    @Override
    public void removeRelationAttr(List<AttrGroupRelationVO> relationList) {
        List<AttrAttrgroupRelationEntity> relationEntityList = relationList.stream().map((relation) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtil.copyProperties(relation, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationService.removeBatchRelation(relationEntityList);
    }


}