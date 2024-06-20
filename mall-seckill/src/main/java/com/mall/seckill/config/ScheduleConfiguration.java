package com.mall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类，开启定时任务，开启异步调度
 */
@EnableAsync
@EnableScheduling
@Configuration
public class ScheduleConfiguration {
}
