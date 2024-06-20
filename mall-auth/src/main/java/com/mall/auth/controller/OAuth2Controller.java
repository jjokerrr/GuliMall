package com.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.mall.auth.client.MemberClient;
import com.mall.auth.config.properties.OAuth2WeiboProperties;
import com.mall.common.constant.AuthConstant;
import com.mall.common.constant.UrlConstant;
import com.mall.common.vo.MemberEntityVO;
import com.mall.auth.vo.SocialUser;
import com.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class OAuth2Controller {
    @Resource
    private MemberClient memberClient;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private OAuth2WeiboProperties oAuth2WeiboProperties;

    @GetMapping("/oauth2.0/weibo/success")
    public String weiboLogin(@RequestParam("code") String code, HttpSession session) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("client_id", oAuth2WeiboProperties.getClientId());
        request.add("client_secret", oAuth2WeiboProperties.getClientSecret());
        request.add("grant_type", oAuth2WeiboProperties.getGrantType());
        request.add("code", code);
        request.add("redirect_uri", UrlConstant.WEIBO_SUCCESS_URL);

        // 创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 创建HttpEntity对象，包含请求体和请求头
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(request, headers);

        SocialUser socialUser = restTemplate.postForObject(UrlConstant.WEIBO_TOKEN_URL, requestEntity, SocialUser.class);
        // 请求失败，回到登录页
        if (socialUser == null) return "redirect:" + UrlConstant.LOGIN_URL;
        String accessToken = socialUser.getAccess_token();

        // 登录
        R response = memberClient.authLogin(socialUser);
        if (!response.getCode().equals("0")) {
            log.error("登入失败{}", response.getMsg());
            return "redirect:" + UrlConstant.LOGIN_URL;
        }

        MemberEntityVO member = response.getData("data", new TypeReference<MemberEntityVO>() {
        });
        session.setAttribute(AuthConstant.SESSION_USER, member);
        // 登录成功返回首页
        return "redirect:" + UrlConstant.INDEX_URL;
    }
}
