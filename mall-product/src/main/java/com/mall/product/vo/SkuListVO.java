package com.mall.product.vo;

import lombok.Data;

@Data
public class SkuListVO {
    private Long catalogId;
    private Long brandId;
    private String min;
    private String max;
}
