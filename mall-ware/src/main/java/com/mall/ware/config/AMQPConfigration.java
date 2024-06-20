package com.mall.ware.config;

import com.mall.common.constant.WareMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class AMQPConfigration {

    /**
     * 提供json消息转换器，提供给rabbitTemplate消息序列化
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 库存服务默认的交换机
     */
    @Bean
    public Exchange stockEventExchange() {
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return ExchangeBuilder.topicExchange(WareMqConstant.STOCK_EVENT_EXCHANGE).durable(true).build();
    }

    /**
     * 普通队列
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        return QueueBuilder.durable(WareMqConstant.STOCK_RELEASE_STOCK_QUEUE).build();
    }


    /**
     * 延迟队列
     */
    @Bean
    public Queue stockDelayQueue() {

        return QueueBuilder.durable(WareMqConstant.STOCK_DELAY_QUEUE)
                .deadLetterExchange(WareMqConstant.STOCK_EVENT_EXCHANGE)
                .deadLetterRoutingKey(WareMqConstant.STOCK_RELEASE)
                .ttl(120000)
                .build();
    }


    /**
     * 交换机与普通队列绑定
     */
    @Bean
    public Binding stockLocked(Queue stockReleaseStockQueue, Exchange stockEventExchange) {

        return BindingBuilder.bind(stockReleaseStockQueue).to(stockEventExchange).with(WareMqConstant.STOCK_RELEASE + ".#").noargs();
    }


    /**
     * 交换机与延迟队列绑定
     */
    @Bean
    public Binding stockLockedBinding(Queue stockDelayQueue, Exchange stockEventExchange) {
        return BindingBuilder.bind(stockDelayQueue).to(stockEventExchange).with(WareMqConstant.STOCK_LOCKED).noargs();
    }
}
