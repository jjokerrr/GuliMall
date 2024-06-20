package com.mall.seckill.schedule;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DemoScheDuleTest {
    // 定时任务是阻塞的，当前定时任务阻塞之后，其他的定时任务会阻塞
    // 避免阻塞

    /**
     * 1. 使用线程池异步执行
     * 2. 修改配置，spring.task.scheduling.pool.size = x 增加异步线程池数量
     * 3. 使用异步任务功能，标记Async注解
     */


//    @Scheduled(cron = "*/2 * * * * *")
//    public void testSche() {
//        log.info("输出定时任务");
//
//    }
    @Test
    public void test() {
        System.out.println("Hello World!");
    }
}
