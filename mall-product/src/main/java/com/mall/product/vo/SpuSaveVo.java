/**
 * Copyright 2019 bejson.com
 */
package com.mall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2019-11-26 10:50:34
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo {

    private String spuName;
    private String spuDescription;
    private Long catelogId;
    private Long brandId;
    private BigDecimal weight;      // 对于高精度的小数字段，一般使用BigDecimal来代替Float或者Double
    private int publishStatus;
    private List<String> decript;
    private List<Images> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Sku> skus;
}