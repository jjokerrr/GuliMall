package com.mall.member.vo;

import lombok.Data;

@Data
public class MemberUserLoginVo {

    /**
     * 登录账户(手机号或者用户名)
     */
    private String loginacct;

    /**
     * 登录密码(加密)
     */
    private String password;

}
