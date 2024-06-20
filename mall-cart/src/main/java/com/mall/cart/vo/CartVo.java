package com.mall.cart.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车存放的商品信息   需要计算的属性需要重写get方法，保证每次获取属性都会进行计算
 **/
@Data
public class CartVo {

    /**
     * 购物车子项信息
     */
    List<CartItemVo> items;

    /**
     * 商品数量
     */
    private Integer countNum;

    /**
     * 商品类型数量
     */
    private Integer countType;

    /**
     * 商品总价
     */
    private BigDecimal totalAmount;

    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal("0.00");
    ;


    public Integer getCountNum() {
        int count = 0;
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItemVo item : items) {
                count += 1;
            }
        }
        return count;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        // 计算购物项总价
        if (!CollectionUtils.isEmpty(items)) {
            amount = items.stream().filter(CartItemVo::getCheck).map(CartItemVo::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        // 计算优惠后的价格
        return amount.subtract(getReduce());
    }

}
