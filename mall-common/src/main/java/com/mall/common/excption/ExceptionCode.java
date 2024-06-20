package com.mall.common.excption;

public enum ExceptionCode {
    UNKNOW_EXCEPTION_CODE(10000, "未知异常"),
    VALID_EXCEPTION_CODE(10001, "参数格式校验错误"),

    PRODUCT_UP_EXCEPTION_CODE(11000, "商品上架失败");


    private int code;
    private String msg;

    ExceptionCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
