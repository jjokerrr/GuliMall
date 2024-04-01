package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.AttrEntity;
import com.mall.product.vo.AttrRespVO;
import com.mall.product.vo.AttrVO;

import java.util.Map;

/**
 * 商品属性
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:48
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<AttrEntity> query);

    void saveAttr(AttrVO attr);

    PageUtils queryBaseList(Long catelogId, Map<String, Object> params);

    AttrRespVO getAttrInfo(Long attrId);

    void updateAttr(AttrVO attr);

    PageUtils querySaleList(Long catelogId, Map<String, Object> params);

}

