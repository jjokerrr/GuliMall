package com.mall.cart.service;

import com.mall.cart.vo.CartItemVo;
import com.mall.cart.vo.CartVo;

import java.util.List;

public interface CartService {


    void addToCart(Long skuId, Integer num);

    CartItemVo getCartItem(Long skuId);

    CartVo getCartList();

    void updateCartItemCount(Long skuId, Integer num);

    void deleteCartItem(Long skuId);

    List<CartItemVo> queryCartByUserId(Long userId);
}
