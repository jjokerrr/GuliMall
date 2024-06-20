package com.mall.common.excption;

public class NoExistOrderException extends RuntimeException {
    private static final String msg = "order not exist";

    public NoExistOrderException() {
        super(msg);
    }
}