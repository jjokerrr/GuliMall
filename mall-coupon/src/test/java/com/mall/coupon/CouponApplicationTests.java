package com.mall.coupon;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.mall.coupon.entity.SeckillSessionEntity;
import com.mall.coupon.service.SeckillSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
class CouponApplicationTests {
    @Resource
    private SeckillSessionService seckillSessionService;

    @Test
    void contextLoads() {
    }

    @Test
    public void testPeriodSession(){
        List<SeckillSessionEntity> seckillSessionEntities = seckillSessionService.querySessionbyLastDays(2);
        System.out.println(seckillSessionEntities);
    }

    @Test
    public void testTime(){
        LocalDateTime now = LocalDateTimeUtil.now();
        LocalDateTime end = now.plusDays(3);
        LocalDateTime beginDay = LocalDateTimeUtil.beginOfDay(now);
        LocalDateTime endDay = LocalDateTimeUtil.beginOfDay(end);
        System.out.println(beginDay);
        System.out.println(endDay);
    }



}
