package com.mall.seckill.schedule;

import com.mall.seckill.service.SeckillScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SeckillSkuSchedule {
    @Resource
    private SeckillScheduleService seckillScheduleService;

    @Scheduled(cron = "0 */10 * * * *")
    public void uploadSeckillProducts() {
        log.info("正在上架最近三天的商品");
        seckillScheduleService.uploadSeckillSku3LastDays();
    }
}
