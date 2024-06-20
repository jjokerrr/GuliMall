package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.SkuSaleAttrValueEntity;
import com.mall.product.vo.SkuItemSaleAttrVO;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:48
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);


    List<SkuItemSaleAttrVO> querySaleAttrCombinationBySkuList(Collection<? extends Serializable> skuLists);

    List<String> getSaleAttrsBySkuId(Long skuId);
}

