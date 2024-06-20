package com.mall.auth.utils;

import cn.hutool.core.util.RandomUtil;

/**
 * 验证码
 */
public class CodeUtils {
    /**
     * 生成digits位数的随机验证码
     */
    public static String generateCode(int digits) {
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;
        int randomCode = RandomUtil.randomInt(min, max);
        StringBuilder code = new StringBuilder(String.valueOf(randomCode));
        // 检查长度是否满足要求
        if (code.length() < digits) {
            // 计算需要补充的0的数量
            int zeroCount = digits - code.length();
            // 在字符串前面补0
            for (int i = 0; i < zeroCount; i++) {
                code.insert(0, "0");
            }
        }
        return String.valueOf(code);
    }
}
