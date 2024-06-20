package com.mall.common.interceptor;

import com.mall.common.constant.AuthConstant;
import com.mall.common.utils.UserHolder;
import com.mall.common.vo.MemberEntityVO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 本地获取登录状态
        MemberEntityVO user = UserHolder.getUser();
        if (user != null) return true;
        // 获取session中的内容
        HttpSession session = request.getSession();
        MemberEntityVO memberEntityVO = (MemberEntityVO) session.getAttribute(AuthConstant.SESSION_USER);
        // 尚未登录，放行由后续逻辑处理
        if (memberEntityVO == null) {
            return true;
        }
        UserHolder.setUser(memberEntityVO);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 方法执行结束之后清除用户状态，避免内存泄露
        UserHolder.removeUser();
    }
}
