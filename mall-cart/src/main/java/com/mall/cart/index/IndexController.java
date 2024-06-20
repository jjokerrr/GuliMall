package com.mall.cart.index;

import cn.hutool.core.bean.BeanUtil;
import com.mall.cart.service.CartService;
import com.mall.common.utils.UserHolder;
import com.mall.cart.vo.CartItemVo;
import com.mall.cart.vo.CartVo;
import com.mall.common.constant.UrlConstant;
import com.mall.common.vo.MemberEntityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class IndexController {

    @Resource
    private CartService cartService;

    @GetMapping("/cart.html")
    public String index(Model model, HttpSession session) {
        MemberEntityVO loginUser = UserHolder.getUser();
        // 未登录用户访问购物车跳转到登录页面
        if (BeanUtil.isEmpty(loginUser)) {
            return "redirect:" + UrlConstant.LOGIN_URL;
        }

        CartVo cartVo = cartService.getCartList();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }



    /**
     * 跳转到添加购物车成功页面
     */
    @GetMapping(value = "/addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,
                                       Model model) {
        //重定向到成功页面。再次查询购物车数据即可
        CartItemVo cartItemVo = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItemVo);
        return "success";
    }


}
