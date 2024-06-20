package com.mall.seckill;

import com.mall.seckill.service.SeckillScheduleService;
import com.mall.seckill.service.SeckillService;
import com.mall.seckill.to.SeckillSkuRedisTo;
import com.mall.seckill.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class SeckillApplicationTest {
    @Resource
    private SeckillScheduleService seckillScheduleService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private SeckillService seckillService;

    @Test
    public void testUpload() {
        seckillScheduleService.uploadSeckillSku3LastDays();
    }

    @Test
    public void testKeys() {
        Set<String> strings = redisUtil.scanKeys("seckill:session:*_*");
        System.out.println(strings);
    }

    @Test
    public void getCurrentSku() {
        List<SeckillSkuRedisTo> currentSeckillInfo = seckillService.getCurrentSeckillInfo();
        System.out.println(currentSeckillInfo);
    }


}
