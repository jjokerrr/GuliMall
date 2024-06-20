package com.mall.auth.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oauth2.weibo")
public class OAuth2WeiboProperties {
    private String clientId;
    private String clientSecret;
    private String grantType;
}
