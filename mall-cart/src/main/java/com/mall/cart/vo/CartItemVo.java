package com.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项内容(购物车中单个商品)
 */
@Data
public class CartItemVo {

    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 是否选中
     */
    private Boolean check = true;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片
     */
    private String image;

    /**
     * 商品套餐属性（sku销售属性信息）
     */
    private List<String> skuAttrValues;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 总价
     */
    private BigDecimal totalPrice;

    /**
     * 计算当前购物项总价
     */
    public BigDecimal getTotalPrice() {

        return this.price.multiply(new BigDecimal("" + this.count));
    }
}
