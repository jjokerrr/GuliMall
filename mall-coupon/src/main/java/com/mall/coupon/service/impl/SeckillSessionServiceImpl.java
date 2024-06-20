package com.mall.coupon.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.coupon.entity.SeckillSessionEntity;
import com.mall.coupon.entity.SeckillSkuRelationEntity;
import com.mall.coupon.mapper.SeckillSessionMapper;
import com.mall.coupon.service.SeckillSessionService;
import com.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionMapper, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询最近days天数的秒杀商品
     */
    @Override
    public List<SeckillSessionEntity> querySessionbyLastDays(Integer days) {
        LocalDateTime now = LocalDateTimeUtil.now();
        LocalDateTime end = now.plusDays(days);
        LocalDateTime beginDay = LocalDateTimeUtil.beginOfDay(now);
        LocalDateTime endDay = LocalDateTimeUtil.beginOfDay(end);
        List<SeckillSessionEntity> seckillSessionEntities = this.baseMapper.querySessionByDays(beginDay, endDay);
        seckillSessionEntities.forEach(seckillSessionEntity -> {
            List<SeckillSkuRelationEntity> list = seckillSkuRelationService.lambdaQuery()
                    .eq(SeckillSkuRelationEntity::getPromotionSessionId, seckillSessionEntity.getId())
                    .list();
            seckillSessionEntity.setRelationSkus(list);
        });
        return seckillSessionEntities;
    }

}