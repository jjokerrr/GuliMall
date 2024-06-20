package com.mall.member.vo;

import lombok.Data;


@Data
public class MemberUserRegisterVo {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

}
