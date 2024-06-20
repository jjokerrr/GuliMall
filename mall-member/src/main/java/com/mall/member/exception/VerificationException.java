package com.mall.member.exception;

/**
 * 数据校验异常，重复值，非法值等
 */
public class VerificationException extends RuntimeException {
    public VerificationException(String msg) {
        super(msg);
    }

}
