package com.mall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 网关跨域请求配置
 *
 * @Parameter
 * @Return null
 */

@Configuration
public class MallGorsConfigration {
    @Bean
    public CorsWebFilter corsWebFilter() {
        // 配置跨域网关
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 配置跨域规则
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);

        // 根据路由注册跨域规则
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
