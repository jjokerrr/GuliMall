package com.mall.order.mapper;

import com.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:39:19
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
