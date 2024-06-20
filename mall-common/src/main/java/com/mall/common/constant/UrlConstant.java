package com.mall.common.constant;

public class UrlConstant {
    /**
     * 微博授权accessToken页面url
     */
    public static final String WEIBO_TOKEN_URL = "https://api.weibo.com/oauth2/access_token";

    /**
     * weibo 授权正确回调
     */
    public static final String WEIBO_SUCCESS_URL = "http://auth.mall.com/oauth2.0/weibo/success";

    /**
     * weibo 授权异常回调
     */
    public static final String WEIBO_ERROR_URL = "http://auth.mall.com/oauth2.0/weibo/error";

    /**
     * 首页url
     */
    public static final String INDEX_URL = "http://mall.com";


    /**
     * 登录页url
     */
    public static final String LOGIN_URL = "http://auth.mall.com/login.html";

    /**
     * 购物车页面
     */
    public static final String CART_URL = "http://cart.mall.com/cart.html";


    /**
     * 注册页url
     */
    public static final String REGISTER_URL = "http://auth.mall.com/register.html";

    /**
     * 添加购物车成功页面
     */
    public static final String ADD_CART_SUCCESS_URL = "http://cart.mall.com/addToCartSuccessPage.html";


    /**
     * 订单结算页面
     */
    public static final String CONFIRM_ORDER_URL = "http://order.mall.com/toTrade";
}
