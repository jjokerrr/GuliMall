package com.mall.ware.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.ware.entity.WareOrderTaskDetailEntity;
import com.mall.ware.mapper.WareOrderTaskDetailMapper;
import com.mall.ware.mapper.WareSkuMapper;
import com.mall.ware.service.WareOrderTaskDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareOrderTaskDetailService")
public class WareOrderTaskDetailServiceImpl extends ServiceImpl<WareOrderTaskDetailMapper, WareOrderTaskDetailEntity> implements WareOrderTaskDetailService {

    @Resource
    private WareSkuMapper wareSkuMapper;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskDetailEntity> page = this.page(
                new Query<WareOrderTaskDetailEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public Boolean rollBackLockTasks(List<Long> orderTaskDetailIds) {

        if (CollectionUtil.isEmpty(orderTaskDetailIds)) {
            return true;
        }
        boolean success = true;
        List<WareOrderTaskDetailEntity> wareOrderTaskEntities = this.listByIds(orderTaskDetailIds);
        // 解锁库存
        for (WareOrderTaskDetailEntity wareOrderTaskDetail : wareOrderTaskEntities) {
            if (wareOrderTaskDetail.getLockStatus() == 1) {
                Long wareId = wareOrderTaskDetail.getWareId();
                Long skuId = wareOrderTaskDetail.getSkuId();
                Integer skuNum = wareOrderTaskDetail.getSkuNum();
                success = success && wareSkuMapper.releaseStock(wareId, skuId, skuNum);
                wareOrderTaskDetail.setLockStatus(2);
            }
        }
        // 更新详情工作单状态
        this.updateBatchById(wareOrderTaskEntities);
        return success;

    }

    /**
     * 更新工作单
     */
    @Override
    public void confirmWareTaskDetail(List<Long> orderTaskDetailIds) {
        // 更新工作单细节
        this.lambdaUpdate().set(WareOrderTaskDetailEntity::getLockStatus, 3).in(WareOrderTaskDetailEntity::getId, orderTaskDetailIds);
        // 修改库存
        List<WareOrderTaskDetailEntity> wareOrderTaskEntities = this.listByIds(orderTaskDetailIds);
        // 解锁库存
        for (WareOrderTaskDetailEntity wareOrderTaskDetail : wareOrderTaskEntities) {
            Long wareId = wareOrderTaskDetail.getWareId();
            Long skuId = wareOrderTaskDetail.getSkuId();
            Integer skuNum = wareOrderTaskDetail.getSkuNum();
            // 解锁锁定库存并且减少库存
            wareSkuMapper.deductStock(wareId, skuId, skuNum);
        }
    }

    /**
     * 根据工作单id查询全部子工作单id
     */
    @Override
    public List<Long> getDetailIdsBytaskId(Long taskId) {
        return this.lambdaQuery().eq(WareOrderTaskDetailEntity::getTaskId, taskId).list().stream().map(WareOrderTaskDetailEntity::getId).collect(Collectors.toList());
    }

}