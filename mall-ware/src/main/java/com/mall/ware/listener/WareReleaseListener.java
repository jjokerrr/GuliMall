package com.mall.ware.listener;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.TypeReference;
import com.mall.common.to.OrderTo;
import com.mall.common.to.mq.StockLockedTo;
import com.mall.common.utils.R;
import com.mall.ware.OrderClient;
import com.mall.common.constant.WareMqConstant;
import com.mall.ware.entity.WareOrderTaskEntity;
import com.mall.ware.service.WareOrderTaskDetailService;
import com.mall.ware.service.WareOrderTaskService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class WareReleaseListener {
    @Resource
    private OrderClient orderClient;
    @Resource
    private WareOrderTaskService wareOrderTaskService;

    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;


    @RabbitHandler
    @RabbitListener(queues = WareMqConstant.STOCK_RELEASE_STOCK_QUEUE)
    public void releaseWareLock(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        // 不存在相关订单项，说明主下单流程回滚，那么数据需要回滚
        // 存在订单项，订单项取消状态，锁定库存需要回滚
        // 存在订单项，订单正常状态，不需要解锁库存，进入支付流程
        Long wareOrderTaskId = stockLockedTo.getId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(wareOrderTaskId);
        if (wareOrderTaskEntity == null) {
            return;
        }
        String orderSn = wareOrderTaskEntity.getOrderSn();
        R orderResponse = orderClient.queryOrderByOrderSn(orderSn);
        if (!orderResponse.getCode().equals("0")) {
            //  订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】
            log.info("{}: 接收到超时库存锁定信息，准备解锁，任务单{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), stockLockedTo.getId());
            rollBackAndAck(stockLockedTo, channel, deliveryTag);
            return;
        }
        OrderTo order = orderResponse.getData("data", new TypeReference<OrderTo>() {
        });
        if (order.getStatus() == 0 || order.getStatus() == 4) {
            //  订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】
            log.info("{}: 接收到超时库存锁定信息，准备解锁，任务单{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), stockLockedTo.getId());
            rollBackAndAck(stockLockedTo, channel, deliveryTag);
            return;
        }
//        confirmWareTaskDetail(stockLockedTo.getOrderTaskDetailIds());
        channel.basicAck(deliveryTag, false);


    }

    private void confirmWareTaskDetail(List<Long> orderTaskDetailIds) {
        wareOrderTaskDetailService.confirmWareTaskDetail(orderTaskDetailIds);
    }

    /**
     * 回滚并且ack，如果回复失败的情况需要将消息重新写入队列
     */
    private void rollBackAndAck(StockLockedTo stockLockedTo, Channel channel, long deliveryTag) throws IOException {
        Boolean b = rollBackLockStatus(stockLockedTo.getOrderTaskDetailIds());
        if (BooleanUtil.isFalse(b)) {
            channel.basicNack(deliveryTag, false, true);
            return;
        }
        channel.basicAck(deliveryTag, false);
    }

    private Boolean rollBackLockStatus(List<Long> orderTaskDetailIds) {
        return wareOrderTaskDetailService.rollBackLockTasks(orderTaskDetailIds);
    }
}
