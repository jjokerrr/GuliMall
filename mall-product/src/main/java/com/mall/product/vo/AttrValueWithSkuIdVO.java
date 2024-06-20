package com.mall.product.vo;

import lombok.Data;



@Data
public class AttrValueWithSkuIdVO {

    /**
     * 销售属性值
     */
    private String attrValue;

    /**
     * 拥有此销售属性的全部skuId，相当于建立倒赔索引
     */
    private String skuIds;

}
