package com.mall.common.utils;

import com.mall.common.vo.MemberEntityVO;

public class UserHolder {
    private static final ThreadLocal<MemberEntityVO> userHolder = new ThreadLocal<>();


    /**
     * 设置user
     */
    public static void setUser(MemberEntityVO member) {
        userHolder.set(member);
    }


    /**
     * 获取user
     */
    public static MemberEntityVO getUser() {
        return userHolder.get();
    }

    /**
     * 移除user
     */
    public static void removeUser() {
        userHolder.remove();
    }


}
