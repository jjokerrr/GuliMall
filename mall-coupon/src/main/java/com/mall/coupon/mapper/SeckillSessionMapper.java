package com.mall.coupon.mapper;

import com.mall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀活动场次
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:43:52
 */
@Mapper
public interface SeckillSessionMapper extends BaseMapper<SeckillSessionEntity> {

    List<SeckillSessionEntity> querySessionByDays(@Param("beginDay") LocalDateTime beginDay, @Param("endDay") LocalDateTime endDay);
}
