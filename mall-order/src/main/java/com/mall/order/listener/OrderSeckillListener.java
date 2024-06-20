package com.mall.order.listener;

import com.mall.common.constant.OrderMqConstant;
import com.mall.common.to.mq.SeckillOrderTo;
import com.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@RabbitListener(queues = OrderMqConstant.ORDER_CREATE_SECKILL_ORDER_QUEUE)
public class OrderSeckillListener {
    @Resource
    private OrderService orderService;

    @RabbitHandler
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println(seckillOrderTo);
        orderService.createSeckillOrder(seckillOrderTo);
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
