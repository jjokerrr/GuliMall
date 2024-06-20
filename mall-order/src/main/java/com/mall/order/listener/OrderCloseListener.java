package com.mall.order.listener;

import com.mall.common.utils.R;
import com.mall.order.client.WareClient;
import com.mall.common.constant.OrderMqConstant;
import com.mall.order.entity.OrderEntity;
import com.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class OrderCloseListener {

    @Resource
    private OrderService orderService;

    @Resource
    private WareClient wareClient;

    // TODO:在关闭订单的时候，需要分辨是否是秒杀订单，如果是秒杀订单的话需要回填对应的信号量
    @RabbitListener(queues = OrderMqConstant.ORDER_RELEASE_ORDER_QUEUE)
    public void releaseOrder(OrderEntity order, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        order = orderService.getById(order.getId());
        if (order.getStatus() != 0) {
            // 订单已经被消费
            return;
        }

        log.info("{}: 接收到超时订单，准备关闭，订单号{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), order.getOrderSn());
        // 订单超时
        order.setStatus(4);
        orderService.updateById(order);
        // 关闭订单锁定的库存内容
        String orderSn = order.getOrderSn();
        R wareResponse = wareClient.releaseOrderByOrderSn(orderSn);
        if (!wareResponse.getCode().equals("0")) {
            // 重新入队
            channel.basicNack(deliveryTag, false, true);

        }
        channel.basicAck(deliveryTag, false);

    }


}