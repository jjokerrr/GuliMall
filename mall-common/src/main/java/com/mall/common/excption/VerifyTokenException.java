package com.mall.common.excption;

public class VerifyTokenException extends RuntimeException {
    private static final String msg = "invalid order token";

    public VerifyTokenException() {
        super(msg);
    }
}
