package com.mall.order.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AliPayProperties {
    /**
     * 客户应用私钥
     */
    String privateKey;
    /**
     * 支付宝公钥
     */
    String alipayPublicKey;

    /**
     * 支付宝网关地址（可以填写支付宝沙箱网关地址）
     */
    String serverUrl;

    /**
     * 客户应用appid
     */
    String appId;


}
