package com.mall.common.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;



@Data
public class SkuEsModel {
    /**
     * skuId
     */
    private Long skuId;

    /**
     * spuId
     */
    private Long spuId;

    /**
     * 商品标题,检索字段
     */
    private String skuTitle;

    /**
     * 商品价格，keyword字段
     */
    private BigDecimal skuPrice;

    /**
     * 默认图片，非索引字段，非正排字段
     */
    private String skuImg;

    /**
     * 销量
     */
    private Long saleCount;

    /**
     * 是否还有库存
     */
    private Boolean hasStock;

    /**
     * 热度
     */
    private Long hotScore;

    /**
     * 品牌id
     */
    private Long brandId;

    /**
     * 类目id
     */
    private Long catalogId;

    /**
     * 品牌名称，keyword字段
     */
    private String brandName;

    /**
     * 品牌图像，keyword字段
     */
    private String brandImg;

    /**
     * 类目名称,，keyword字段
     */
    private String catalogName;

    /**
     * spu对应属性,nested嵌入字段，如果列表元素是对象的话，会产生
     */
    private List<Attrs> attrs;

    @Data
    public static class Attrs {

        private Long attrId;

        private String attrName;

        private String attrValue;

    }


}
