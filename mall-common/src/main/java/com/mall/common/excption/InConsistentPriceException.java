package com.mall.common.excption;

public class InConsistentPriceException extends RuntimeException {
    private static final String msg = "inconsistent price";

    public InConsistentPriceException() {
        super(msg);
    }
}
