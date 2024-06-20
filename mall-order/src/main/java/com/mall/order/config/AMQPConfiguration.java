package com.mall.order.config;

import com.mall.common.constant.OrderMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class AMQPConfiguration {


    /**
     * 提供json消息转换器，提供给rabbitTemplate消息序列化
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    /**
     * 创建延时队列
     */
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(OrderMqConstant.ORDER_DELAY_QUEUE)
                .deadLetterExchange(OrderMqConstant.ORDER_EVENT_EXCHANGE)
                .deadLetterRoutingKey(OrderMqConstant.ORDER_RELEASE_ORDER_ROUTING_KEY)
                .ttl(120000)
                .build();
    }

    /**
     * 创建死信队列，用于处理延时消息
     */
    @Bean
    public Queue orderReleaseOrderQueue() {
        return QueueBuilder.durable(OrderMqConstant.ORDER_RELEASE_ORDER_QUEUE).build();
    }

    /**
     * 创建秒杀订单队列
     */
    @Bean
    public Queue orderSeckillQueue() {
        return QueueBuilder.durable(OrderMqConstant.ORDER_CREATE_SECKILL_ORDER_QUEUE).build();
    }

    /**
     * order服务交换机
     */
    @Bean
    public Exchange orderEventExchange() {
        return ExchangeBuilder.topicExchange(OrderMqConstant.ORDER_EVENT_EXCHANGE).build();
    }

    @Bean
    public Binding orderDelayBinding(Exchange orderEventExchange, Queue orderDelayQueue) {
        return BindingBuilder.bind(orderDelayQueue).to(orderEventExchange).with(OrderMqConstant.ORDER_CREATE_ORDER_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding orderReleaseBinding(Exchange orderEventExchange, Queue orderReleaseOrderQueue) {
        return BindingBuilder.bind(orderReleaseOrderQueue).to(orderEventExchange).with(OrderMqConstant.ORDER_RELEASE_ORDER_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding orderSeckillBinding(Exchange orderEventExchange, Queue orderSeckillQueue) {
        return BindingBuilder.bind(orderSeckillQueue).to(orderEventExchange).with(OrderMqConstant.ORDER_CREATE_SECKILL_ORDER_ROUTING_KEY).noargs();
    }

}
