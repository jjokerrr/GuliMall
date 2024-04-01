package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.vo.SkuListVO;

import java.util.Map;

/**
 * sku信息
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:48
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<SkuInfoEntity> query);

    PageUtils querySkuList(Map<String, Object> params, SkuListVO skuListVO);
}

