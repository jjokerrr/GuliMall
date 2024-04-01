package com.mall.product.vo;

import lombok.Data;

@Data
public class SpuListVO {
    /**
     * 上架状态
     */
    private Integer status;
    /**
     * 分类Id
     */
    private Long catelogId;
    /**
     * 品牌Id
     */
    private Long brandId;
}
