package com.mall.product.vo;

import lombok.Data;

@Data
public class SkuListVO {
    private Long catelogId;
    private Long brandId;
    private String min;
    private String max;
}
