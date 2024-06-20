package com.mall.cart.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mall.cart.service.CartService;
import com.mall.cart.vo.CartItemVo;
import com.mall.common.constant.UrlConstant;
import com.mall.common.utils.R;
import com.mall.common.utils.UserHolder;
import com.mall.common.vo.MemberEntityVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class CartController {

    @Resource
    private CartService cartService;


    /**
     * 通过用户id查询购物车信息
     */
    @GetMapping("/cart/{userId}")
    @ResponseBody
    public R queryCartById(@PathVariable("userId") Long userId) {
        List<CartItemVo> cartItemVoList = cartService.queryCartByUserId(userId);
        return R.ok().put("data", cartItemVoList);
    }

    /**
     * 将商品添加到购物车
     */
    @GetMapping(value = "/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId,
                              @RequestParam("num") Integer num,
                              RedirectAttributes attributes) {
        MemberEntityVO loginUser = UserHolder.getUser();
        // 未登录添加购物车重定向到登录页面
        if (BeanUtil.isEmpty(loginUser))
            return "redirect:" + UrlConstant.LOGIN_URL;

        cartService.addToCart(skuId, num);

        attributes.addAttribute("skuId", skuId);
        // 采用重定向到成功页面，避免刷新导致的商品重复提交
        return "redirect:" + UrlConstant.ADD_CART_SUCCESS_URL;
    }

    /**
     * 修改商品数量  http://cart.mall.com/countItem?skuId=30&num=2
     */
    @GetMapping(value = "/countItem")
    public String updateCartItemCount(@RequestParam("skuId") Long skuId,
                                      @RequestParam("num") Integer num) {
        MemberEntityVO loginUser = UserHolder.getUser();
        // 未登录添加购物车重定向到登录页面
        if (BeanUtil.isEmpty(loginUser))
            return "redirect:" + UrlConstant.LOGIN_URL;

        cartService.updateCartItemCount(skuId, num);

        // 采用重定向到成功页面，避免刷新导致的商品重复提交
        return "redirect:" + UrlConstant.CART_URL;
    }

    /**
     * 删除商品
     */
    @GetMapping(value = "/deleteItem")
    public String deleteCartItem(@RequestParam("skuId") Long skuId) {
        MemberEntityVO loginUser = UserHolder.getUser();
        // 未登录添加购物车重定向到登录页面
        if (BeanUtil.isEmpty(loginUser))
            return "redirect:" + UrlConstant.LOGIN_URL;

        cartService.deleteCartItem(skuId);

        // 采用重定向到成功页面，避免刷新导致的商品重复提交
        return "redirect:" + UrlConstant.CART_URL;
    }

}
