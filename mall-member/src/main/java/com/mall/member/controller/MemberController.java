package com.mall.member.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;
import com.mall.member.constant.MemberConstant;
import com.mall.member.entity.MemberEntity;
import com.mall.member.exception.VerificationException;
import com.mall.member.service.MemberService;
import com.mall.member.vo.MemberUserLoginVo;
import com.mall.member.vo.MemberUserRegisterVo;
import com.mall.member.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 会员
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:46:02
 */
@Slf4j
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberUserRegisterVo registerVo) {
        Boolean success = null;
        try {
            success = memberService.register(registerVo);
        } catch (VerificationException e) {
            return R.error(e.getMessage());
        }
        return success ? R.ok() : R.error("注册失败");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberUserLoginVo loginVo) {
        String account = loginVo.getLoginacct();
        String password = loginVo.getPassword();
        if (StrUtil.isBlank(account) || StrUtil.isBlank(password)) {
            return R.error("请输入正确的用户信息");
        }
        // 验证登录
        MemberEntity member = memberService.login(account, password);
        if (BeanUtil.isEmpty(member)) {
            return R.error("密码错误，请重新登录");
        }
        return R.ok("登录成功").put("data", member);
    }

    /**
     * 社交登录
     */
    @PostMapping("oauth2/login")
    public R authLogin(@RequestBody SocialUser socialUser) {
        // 存储token
        stringRedisTemplate.opsForValue()
                .set(MemberConstant.MEMBER_WEIBO_TOKEN_CACHE_PREFIX + socialUser.getUid()
                        , socialUser.getAccess_token()
                        , socialUser.getExpires_in()
                        , TimeUnit.SECONDS);
        // 登录操作
        MemberEntity member = memberService.login(socialUser);
        return !BeanUtil.isEmpty(member) ? R.ok("登入成功").put("data", member) : R.error("登入失败");
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
