package com.mall.member.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.member.constant.MemberConstant;
import com.mall.member.entity.MemberEntity;
import com.mall.member.exception.VerificationException;
import com.mall.member.mapper.MemberMapper;
import com.mall.member.service.MemberLevelService;
import com.mall.member.service.MemberService;
import com.mall.member.vo.MemberUserRegisterVo;
import com.mall.member.vo.SocialUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberMapper, MemberEntity> implements MemberService {

    @Resource
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 用户注册
     */
    @Override
    public Boolean register(MemberUserRegisterVo registerVo) {
        String phone = registerVo.getPhone();
        String userName = registerVo.getUserName();
        String password = registerVo.getPassword();
        MemberEntity memberEntity = new MemberEntity();
        // 默认nickName
        String nickName = generateNickName();
        memberEntity.setNickname(nickName);
        // 手机号和用户名需要进行登录验证，这里需要确保唯一性
        if (!checkUserNameUnique(userName)) {
            throw new VerificationException("用户名不唯一");
        }
        if (!checkPhoneUnique(phone)) {
            throw new VerificationException("手机号已经注册");
        }
        memberEntity.setUsername(userName);
        memberEntity.setMobile(phone);
        // 进行md5加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassWord = passwordEncoder.encode(password);
        memberEntity.setPassword(encodePassWord);
        memberEntity.setLevelId(memberLevelService.getDefaultLevel());
        memberEntity.setStatus(1);
        return save(memberEntity);
    }

    @NotNull
    private static String generateNickName() {
        return MemberConstant.DEFAULT_NICKNAME_PREFIX + UUID.randomUUID().toString().substring(4);
    }

    @Override
    public Boolean checkUserNameUnique(String userName) {
        if (StrUtil.isBlank(userName)) {
            return false;
        }
        return this.lambdaQuery()
                .eq(MemberEntity::getUsername, userName).count() <= 0;
    }

    @Override
    public Boolean checkPhoneUnique(String phone) {
        if (StrUtil.isBlank(phone)) {
            return false;
        }
        return this.lambdaQuery()
                .eq(MemberEntity::getMobile, phone).count() <= 0;
    }

    @Override
    public MemberEntity login(String account, String password) {
        MemberEntity member = lambdaQuery().eq(MemberEntity::getUsername, account)
                .or()
                .eq(MemberEntity::getMobile, account)
                .one();
        if (member == null) return null;
        String memberPassword = member.getPassword();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder.matches(password, memberPassword) ? member : null;
    }

    /**
     * 第三方社交登录吗，uid为唯一认证表示，若首次登入则创建新用户
     */
    @Override
    public MemberEntity login(SocialUser socialUser) {
        // 检查用户是否存在
        Long count = this.lambdaQuery().eq(MemberEntity::getSocialUid, socialUser.getUid()).count();
        if (count <= 0L) {
            // 用户不存在，注册用户
            MemberEntity memberEntity = new MemberEntity();
            String nickName = generateNickName();
            memberEntity.setNickname(nickName);
            memberEntity.setLevelId(memberLevelService.getDefaultLevel());
            memberEntity.setStatus(1);
            memberEntity.setSocialUid(socialUser.getUid());
            save(memberEntity);
        }
        // 查询用户信息
        return this.lambdaQuery()
                .eq(MemberEntity::getSocialUid, socialUser.getUid())
                .one();
    }


}