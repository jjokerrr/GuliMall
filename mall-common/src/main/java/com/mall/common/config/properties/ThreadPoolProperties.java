package com.mall.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("mall.thread")
public class ThreadPoolProperties {
    /**
     * 核心线程数
     */
    private Integer corePoolSize = 10;
    /**
     * 最大线程数
     */
    private Integer maximumPoolSize = 50;

    /**
     * 最长回收时间，单位毫秒
     */
    private Long keepAliveTime = 1000L;

}
