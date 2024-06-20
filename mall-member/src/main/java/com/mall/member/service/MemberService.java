package com.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.member.entity.MemberEntity;
import com.mall.member.exception.VerificationException;
import com.mall.member.vo.MemberUserRegisterVo;
import com.mall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:46:02
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    Boolean register(MemberUserRegisterVo registerVo) throws VerificationException;

    Boolean checkUserNameUnique(String userName);

    Boolean checkPhoneUnique(String phone);

    MemberEntity login(String account, String password);

    MemberEntity login(SocialUser socialUser);
}

