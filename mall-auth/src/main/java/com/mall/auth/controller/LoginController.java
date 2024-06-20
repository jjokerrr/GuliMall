package com.mall.auth.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.mall.auth.client.MemberClient;
import com.mall.auth.constant.UrlConstant;
import com.mall.auth.utils.CodeUtils;
import com.mall.auth.utils.RegexUtils;
import com.mall.common.vo.MemberEntityVO;
import com.mall.auth.vo.UserLoginVo;
import com.mall.auth.vo.UserRegisterVo;
import com.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Controller     // 由于使用了springmvc的特殊机制，这里不能使用@RestController注解
public class LoginController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MemberClient memberClient;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phoneNumber) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phoneNumber)) {
            log.error("手机号错误{}", phoneNumber);
            return R.error("请输入正确的手机号");
        }
        String code = CodeUtils.generateCode(UrlConstant.CODE_LENGTH);
        // 存储短信验证码
        Boolean codeSet = stringRedisTemplate.opsForValue()
                .setIfAbsent(UrlConstant.REDIS_LOGIN_CODE_PREFIX + phoneNumber, code, 5, TimeUnit.MINUTES);
        if (!codeSet) {
            return R.error("验证码尚未过期，请稍后重试");
        }
        // 可采用aliyun sms短信验证服务向手机短信发送验证码，这里为了简单处理就不用了
        log.info(code);
        return R.ok();
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vos, BindingResult result,
                           RedirectAttributes attributes) {
        if (result.hasErrors()) {

            System.out.println(result);
            // 将键相同的值通过,相连接
            Map<String, String> errorMsg = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage
                            , (existingValue, newValue) -> existingValue + ", " + newValue));
            System.out.println(errorMsg);
            attributes.addFlashAttribute("errors", errorMsg);
            return "redirect:http://auth.mall.com/register.html";
        }

        // 校验验证码
        String code = vos.getCode();
        String codeOrigin = stringRedisTemplate.opsForValue().get(UrlConstant.REDIS_LOGIN_CODE_PREFIX + vos.getPhone());
        if (StrUtil.isBlank(code) || !code.equals(codeOrigin)) {
            log.info("验证码错误");
            HashMap<String, String> error = new HashMap<>();
            error.put("code", "验证码错误");
            attributes.addFlashAttribute("errors", error);
            return "redirect:http://auth.mall.com/register.html";
        }

        // 删除旧验证码信息
        stringRedisTemplate.delete(UrlConstant.REDIS_LOGIN_CODE_PREFIX + vos.getPhone());
        // 保存用户信息  member远程服务
        R register = memberClient.register(vos);
        if (!register.getCode().equals("0")) {
            log.error("注册失败！{}", register.getMsg());
            HashMap<String, String> error = new HashMap<>();
            error.put("msg", register.getMsg());
            attributes.addFlashAttribute("errors", error);
            return "redirect:http://auth.mall.com/register.html";
        }
        log.info("注册成功");
        return "redirect:http://auth.mall.com/login.html";
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes attributes, HttpSession session) {
        // 验证登录
        R login = memberClient.login(userLoginVo);
        if (!login.getCode().equals("0")) {
            // 登录失败，放回登录页面
            log.error("登录失败！{}", login.getMsg());
            HashMap<String, String> error = new HashMap<>();
            error.put("msg", login.getMsg());
            attributes.addFlashAttribute("errors", error);
            return "redirect:http://auth.mall.com/login.html";

        }
        // 添加session
        session.setAttribute("session", login.getData("data", new TypeReference<MemberEntityVO>(){}));
        // 保存用户登录状态
        // 重定向
        return "redirect:http://mall.com";
    }
}
