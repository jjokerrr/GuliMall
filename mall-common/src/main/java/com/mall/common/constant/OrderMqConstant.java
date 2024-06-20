package com.mall.common.constant;

public class OrderMqConstant {
    /**
     * 订单延时队列
     */
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    /**
     * 订单服务交换机
     */
    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";
    /**
     * 订单释放队列路由键
     */
    public static final String ORDER_RELEASE_ORDER_ROUTING_KEY = "order.release.order";
    /**
     * 释放订单队列
     */
    public static final String ORDER_RELEASE_ORDER_QUEUE = "order.release.order.queue";

    /**
     * 创建秒杀订单队列
     */
    public static final String ORDER_CREATE_SECKILL_ORDER_QUEUE = "order.create.seckill.queue";
    /**
     * 创建订单路由键
     */
    public static final String ORDER_CREATE_ORDER_ROUTING_KEY = "order.create.order";

    /**
     * 创建秒杀订单路由键
     */
    public static final String ORDER_CREATE_SECKILL_ORDER_ROUTING_KEY = "order.create.seckill.order";
}
