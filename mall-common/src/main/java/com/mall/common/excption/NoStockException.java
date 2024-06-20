package com.mall.common.excption;

/**
 * 库存不足异常
 */
public class NoStockException extends RuntimeException {
    private static final String ERR_MSG = " insufficient inventory";

    public NoStockException() {
        super(ERR_MSG);
    }
}
