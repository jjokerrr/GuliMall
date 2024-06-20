package com.mall.auth.index;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class IndexController {
    /**
     * 登录页面
     */
    @GetMapping(value = { "/login.html"})
    public String login(Model model) {
        return "login";
    }

    /**
     * 注册页面
     */
    @GetMapping(value = {"register.html"})
    public String register(Model model) {
        return "register";
    }

}
