package com.mall.auth;

import com.mall.auth.constant.UrlConstant;
import com.mall.auth.utils.CodeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AuthApplicationTests {
    @Test
    public void testCode() {
        String s = CodeUtils.generateCode(UrlConstant.CODE_LENGTH);
        System.out.println(s);
    }
}
