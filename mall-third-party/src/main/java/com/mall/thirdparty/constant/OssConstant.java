package com.mall.thirdparty.constant;

import com.aliyun.oss.internal.OSSConstants;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@Component
public class OssConstant {
    @Value("${alibaba.cloud.oss.endpoint}")
    private String endpoint;

    @Value("${alibaba.cloud.oss.bucket}")
    private String bucket;

    @Value("${alibaba.cloud.access-key}")
    private String accessKey;

    @Value("${alibaba.cloud.secret-key}")
    private String secretKey;

    private String host;

    @PostConstruct
    public void init() {
        // 根据注入的属性值设置 host 属性的值
        this.host = OSSConstants.PROTOCOL_HTTPS + bucket + "." + endpoint;
    }


}
