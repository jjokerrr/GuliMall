package com.mall.common.config;

import com.mall.common.config.properties.ThreadPoolProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建自定义线程池对象
 */
@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfiguration {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolProperties threadPoolProperties) {
        return new ThreadPoolExecutor(threadPoolProperties.getCorePoolSize()
                , threadPoolProperties.getMaximumPoolSize()
                , threadPoolProperties.getMaximumPoolSize()
                , TimeUnit.MICROSECONDS
                , new LinkedBlockingQueue<>(1000)
                , Executors.defaultThreadFactory()
                , new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
