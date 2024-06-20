package com.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.to.mq.SeckillOrderTo;
import com.mall.common.utils.PageUtils;
import com.mall.order.entity.OrderEntity;
import com.mall.order.vo.OrderConfirmVo;
import com.mall.order.vo.OrderSubmitVo;
import com.mall.order.vo.SubmitOrderResponseVo;

import java.util.List;
import java.util.Map;

/**
 * 订单
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:39:19
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    SubmitOrderResponseVo submit(OrderSubmitVo orderSubmitVo);

    OrderEntity queryOrderByOrdersn(String orderSn);

    OrderEntity getByOrderSn(String orderSn);

    List<OrderEntity> queryOrderByUserId(Long userId);

    PageUtils queryPageWithItem(Map<String, Object> params,Long userId);

    void processOrderPayment(String orderSn);

    void createSeckillOrder(SeckillOrderTo seckillOrderTo);
}

