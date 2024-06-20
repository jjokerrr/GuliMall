package com.mall.order.client;

import com.mall.common.to.WareSkuStockTO;
import com.mall.common.utils.R;
import com.mall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("mall-ware")
public interface WareClient {

    /**
     * 根据skuId列表查询库存剩余情况
     */
    @PostMapping("ware/waresku/stock")
    List<WareSkuStockTO> queryStockBySkuIds(@RequestBody List<Long> skuIds);

    /**
     * 计算运费信息
     */
    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    /**
     * 锁定商品库存
     */
    @PostMapping("/ware/waresku/order/stock")
    R orderLocks(@RequestBody WareSkuLockVo wareSkuLockVo);

    /**
     * 解锁订单对应的库存锁定
     */
    @GetMapping("/ware/wareordertask/release/{orderSn}")
    R releaseOrderByOrderSn(@PathVariable("orderSn") String orderSn);

    /**
     * 通过订单号扣减库存
     */
    @GetMapping("/ware/wareordertask/deduct")
    R deductWare(@RequestParam("orderSn") String orderSn);

}
