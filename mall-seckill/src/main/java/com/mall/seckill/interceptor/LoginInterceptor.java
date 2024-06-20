package com.mall.seckill.interceptor;

import com.mall.common.constant.UrlConstant;
import com.mall.common.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (UserHolder.getUser() == null) {
            response.sendRedirect("redirect:" + UrlConstant.LOGIN_URL);
            return false;
        }
        return true;

    }
}
