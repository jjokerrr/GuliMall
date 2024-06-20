package com.mall.product.vo;

import com.mall.product.entity.SkuImagesEntity;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 夏沫止水
 * @createTime: 2020-06-19 16:46
 **/


@ToString
@Data
public class SkuItemVO {

    /**
     * sku基本信息
     */
    private SkuInfoEntity info;

    /**
     * 库存信息
     */
    private Boolean hasStock = true;

    /**
     * 图片信息
     */
    private List<SkuImagesEntity> images;

    /**
     * 销售属性总和
     */
    private List<SkuItemSaleAttrVO> saleAttr;

    /**
     * 所属spu介绍
     */
    private SpuInfoDescEntity desc;

    /**
     * 所属spu规格属性信息
     */
    private List<SpuItemAttrGroupVO> groupAttrs;

    /**
     * 秒杀优惠信息
     */
    private SeckillSkuVO seckillSkuVo;

}
