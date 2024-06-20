package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.AttrEntity;
import com.mall.product.entity.AttrGroupEntity;
import com.mall.product.vo.AttrGroupRelationVO;
import com.mall.product.vo.AttrGroupWithAttrVO;
import com.mall.product.vo.SpuItemAttrGroupVO;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:49
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<AttrGroupEntity> queryWrapper);

    PageUtils getPageByCategoryId(Map<String, Object> params, Long categoryId);

    List<AttrEntity> getAllAttr(Long attrGroupId);

    void removeRelationAttr(List<AttrGroupRelationVO> relationList);

    PageUtils getNoRelationAttr(Long attrGroupId, Map<String, Object> params);

    List<AttrGroupWithAttrVO> queryAttrGroupWithAttrByCatalogId(Long catalogId);

    List<SpuItemAttrGroupVO> querySpuItemAttrByCatelogId(Long catelogId);
}


