package com.mall.common.constant;

public class RedisConstant {
    /**
     * 分类树前缀
     */
    public static final String PRODUCT_CATEGORY_INDEX = "product:category:index";

    public static final String PRODUCT_CATEGORY_FLIST = "product:category:flist";
    /**
     * 商品目录前缀
     */
    public static final String PRODUCT_CATEGORY_TREE = "product:category:tree";
    /**
     * 分布式锁前缀
     */
    public static final String LOCK_PREFIX = "lock:";
    /**
     * 用户购物车前缀
     */
    public static final String USER_CART_PREFIX = "cart:user:";
    /**
     * 用户订单幂等性token
     */
    public static final String USER_ORDER_TOKEN_PREFIX = "order:token:";
    /**
     * 秒杀场次前缀
     */
    public static final String SECKILL_SESSION_PREFIX = "seckill:session:";

    /**
     * 秒杀商品前缀
     */
    public static final String SECKILL_SESSION_SKU_PREFIX = "seckill:session:sku:";
    /**
     * 秒杀商品信号量，等同秒杀库存
     */
    public static final String SKU_SEMAPHORE_PREFIX = "seckill:session:semaphore:";

}
