package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.SpuInfoEntity;
import com.mall.product.vo.SpuListVO;
import com.mall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:48
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<SpuInfoEntity> query);

    void saveSpuInfo(SpuSaveVo spuSaveVo);

    PageUtils querySpuList(Map<String, Object> params, SpuListVO spuListVO);

    void up(Long spuId);

    SpuInfoEntity querySpuBySkuId(Long skuId);
}

