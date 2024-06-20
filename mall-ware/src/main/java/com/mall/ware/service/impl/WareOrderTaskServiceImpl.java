package com.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.ware.entity.WareOrderTaskDetailEntity;
import com.mall.ware.entity.WareOrderTaskEntity;
import com.mall.ware.mapper.WareOrderTaskMapper;
import com.mall.ware.service.WareOrderTaskDetailService;
import com.mall.ware.service.WareOrderTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareOrderTaskService")
public class WareOrderTaskServiceImpl extends ServiceImpl<WareOrderTaskMapper, WareOrderTaskEntity> implements WareOrderTaskService {

    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskEntity> page = this.page(
                new Query<WareOrderTaskEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据订单号解锁库存
     */
    @Override
    public Boolean releaseLockedByOrderSn(String orderSn) {
        // 查询订单号对应的订单项
        WareOrderTaskEntity wareOrderTask = this.lambdaQuery().eq(WareOrderTaskEntity::getOrderSn, orderSn).one();
        if (wareOrderTask == null) {
            return true;
        }
        Long taskId = wareOrderTask.getId();
        List<Long> wareOrderTaskDetailIds = wareOrderTaskDetailService.lambdaQuery()
                .eq(WareOrderTaskDetailEntity::getTaskId, taskId)
                .list()
                .stream().map(WareOrderTaskDetailEntity::getId)
                .collect(Collectors.toList());
        return wareOrderTaskDetailService.rollBackLockTasks(wareOrderTaskDetailIds);
    }

    /**
     * 确认订单且扣减库存
     */
    @Override
    public void confirmAndDeduct(String orderSn) {
        Long taskId = this.lambdaQuery().select(WareOrderTaskEntity::getId).eq(WareOrderTaskEntity::getOrderSn, orderSn).one().getId();
        List<Long> detailIds = wareOrderTaskDetailService.getDetailIdsBytaskId(taskId);
        wareOrderTaskDetailService.confirmWareTaskDetail(detailIds);
    }


}