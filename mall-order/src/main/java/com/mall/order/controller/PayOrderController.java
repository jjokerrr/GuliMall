package com.mall.order.controller;

import com.alipay.api.AlipayApiException;
import com.mall.common.excption.NoExistOrderException;
import com.mall.order.entity.OrderEntity;
import com.mall.order.service.OrderService;
import com.mall.order.utils.AlipayTemplate;
import com.mall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.RoundingMode;

@Slf4j
@RestController
public class PayOrderController {
    @Resource
    private AlipayTemplate alipayTemplate;
    @Resource
    private OrderService orderService;

    /**
     * 支付订单
     */
    @GetMapping(value = "/aliPayOrder",produces = "text/html")
    public String payByAlipay(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        // 查询订单
        OrderEntity orderEntity = orderService.getByOrderSn(orderSn);
        if (orderEntity == null || orderEntity.getStatus() != 0) {
            log.error("订单不存在或者订单不处于待支付状态");
            throw new NoExistOrderException();
        }
        PayVo payVo = new PayVo();
        payVo.setSubject("订单"+orderSn);
        payVo.setOut_trade_no(orderSn);
        payVo.setTotal_amount(orderEntity.getPayAmount().setScale(2, RoundingMode.CEILING).toString());
        String pay = alipayTemplate.pay(payVo);
        // 支付成功，修改订单状态
        orderService.processOrderPayment(orderSn);
        return pay;
    }
}
