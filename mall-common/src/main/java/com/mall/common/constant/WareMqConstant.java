package com.mall.common.constant;

public class WareMqConstant {
    /**
     * 仓储服务交换机
     */
    public static final String STOCK_EVENT_EXCHANGE = "stock-event-exchange";
    /**
     * 释放库存锁定队列key
     */
    public static final String STOCK_RELEASE_STOCK_QUEUE = "stock.release.stock.queue";
    /**
     * 库存延时队列（死信队列）
     */
    public static final String STOCK_DELAY_QUEUE = "stock.delay.queue";
    /**
     * 死信routing key
     */
    public static final String STOCK_RELEASE = "stock.release";

    /**
     * Stock Lock
     */
    public static final String STOCK_LOCKED = "stock.locked";

}
