package com.mall.auth.vo;

import lombok.Data;


@Data
public class SocialUser {

    /**
     * 登录token
     */
    private String access_token;

    /**
     * 登录状态维持时间
     */
    private String remind_in;

    /**
     * 登录状态过期时间
     */
    private long expires_in;

    /**
     * 用户uid
     */
    private String uid;


    private String isRealName;

}
