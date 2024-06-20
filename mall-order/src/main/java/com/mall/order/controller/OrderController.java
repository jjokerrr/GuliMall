package com.mall.order.controller;

import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;
import com.mall.order.entity.OrderEntity;
import com.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 订单
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:39:19
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 查找用户的全部订单
     */
    @GetMapping("/list/user")
    public R queryOrderByUserId(@RequestParam("userId") Long userId) {
        List<OrderEntity> orderEntityLists = orderService.queryOrderByUserId(userId);
        return R.ok().put("data", orderEntityLists);
    }

    /**
     * 分页查询当前登录用户的所有订单信息
     */
    @PostMapping("/listWithItem")
    public R listWithItem(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf((String) params.get("userId"));
        PageUtils page = orderService.queryPageWithItem(params, userId);

        return R.ok().put("page", page);
    }

    /**
     * 根据订单号查询订单
     */
    @GetMapping("/query/{orderSn}")
    public R queryOrderByOrderSn(@PathVariable("orderSn") String orderSn) {
        OrderEntity orderEntity = orderService.queryOrderByOrdersn(orderSn);
        if (orderEntity == null) {
            return R.error();
        }
        return R.ok().put("data", orderEntity);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id) {
        OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order) {
        orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order) {
        orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids) {
        orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
