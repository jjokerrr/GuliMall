package com.mall.ware.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.excption.NoStockException;
import com.mall.common.to.WareSkuStockTO;
import com.mall.common.to.mq.StockLockedTo;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.ware.client.ProductClient;
import com.mall.common.constant.WareMqConstant;
import com.mall.ware.entity.WareOrderTaskDetailEntity;
import com.mall.ware.entity.WareOrderTaskEntity;
import com.mall.ware.entity.WareSkuEntity;
import com.mall.ware.mapper.WareSkuMapper;
import com.mall.ware.service.WareOrderTaskDetailService;
import com.mall.ware.service.WareOrderTaskService;
import com.mall.ware.service.WareSkuService;
import com.mall.ware.vo.LockStockResultVo;
import com.mall.ware.vo.OrderItemVo;
import com.mall.ware.vo.WareSkuLockVo;
import com.mall.ware.vo.WareSkuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {


    @Resource
    private ProductClient productClient;
    @Resource
    private WareOrderTaskService wareOrderTaskService;

    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<WareSkuEntity> query) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryWareSkuList(Map<String, Object> params, WareSkuVO wareSkuVO) {
        Long skuId = wareSkuVO.getSkuId();
        Long wareId = wareSkuVO.getWareId();

        String skuIdField = "sku_id";
        String wareIdField = "ware_id";
        QueryWrapper<WareSkuEntity> query = new QueryWrapper<>();
        buildQuery(skuId, query, skuIdField);
        buildQuery(wareId, query, wareIdField);

        return queryPage(params, query);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> wareSkuEntityList = this.baseMapper
                .selectList(new QueryWrapper<WareSkuEntity>()
                        .eq("sku_id", skuId)
                        .eq("ware_id", wareId));
        if (CollectionUtil.isEmpty(wareSkuEntityList)) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            // 还可以用什么办法让异常出现以后不回滚？高级
            // 使用catch-出错时无需回滚
            try {
                R info = productClient.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode().equals("0")) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }


            this.baseMapper.insert(skuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    /**
     * 根据id进行库存的检索，若不存在库存数据，默认设置库存为0
     */
    @Override
    public List<WareSkuStockTO> queryStockBySkuIds(List<Long> skuIds) {
        List<WareSkuStockTO> wareSkuStockTOS = baseMapper.queryStockBySkuIds(skuIds);
        wareSkuStockTOS.forEach(wareSkuStockTO -> {
            wareSkuStockTO.setStock(wareSkuStockTO.getStock() == null ? 0 : wareSkuStockTO.getStock());
        });
        return wareSkuStockTOS;
    }

    /**
     * 锁定商品库存，30m后若未进行付款操作自动解锁库存
     */
    @Transactional
    @Override
    public List<LockStockResultVo> lockWares(WareSkuLockVo wareSkuLockVo) {
        List<OrderItemVo> locks = wareSkuLockVo.getLocks();
        // 创建工作单,将工作单和工作单细节消息
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
        wareOrderTaskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(wareOrderTaskEntity);

        List<WareOrderTaskDetailEntity> wareOrderTaskDetailEntities = new ArrayList<>();
        // 锁库存
        List<LockStockResultVo> lockedResult = locks.stream().map(lock -> {
            LockStockResultVo lockStockResultVo = new LockStockResultVo();
            lockStockResultVo.setSkuId(lock.getSkuId());
            lockStockResultVo.setNum(lock.getCount());
            WareSkuEntity wareSkuEntity = findWareBySkuStockCondition(lock.getSkuId(), lock.getCount());
            if (wareSkuEntity == null) {
                // 不满足库存条件
                log.error("{}，库存不足", lock.getTitle());
                throw new NoStockException();
//                lockStockResultVo.setLocked(false);
//                return lockStockResultVo;
            }
            // 库存充足，锁定库存,查到库存和锁定库存应该是一个原子操作，采用乐观锁机制来扣减库存
            Boolean locked = lockWareStock(wareSkuEntity, lock.getCount());
            if (!locked) {
                // 扣减库存失败
                log.error("{}，扣减库存失败", lock.getTitle());
                throw new NoStockException();
            }
            lockStockResultVo.setLocked(locked);
            WareOrderTaskDetailEntity wareOrderTaskDetailEntity = generateOrderWareTaskDetail(lock, wareOrderTaskEntity.getId(), wareSkuEntity.getWareId());
            wareOrderTaskDetailEntities.add(wareOrderTaskDetailEntity);
            return lockStockResultVo;
        }).collect(Collectors.toList());
        //  保存工作单细节项
        wareOrderTaskDetailService.saveBatch(wareOrderTaskDetailEntities);

        sendLockInfoToMq(lockedResult, wareOrderTaskDetailEntities, wareOrderTaskEntity);

        return lockedResult;

    }

    private void sendLockInfoToMq(List<LockStockResultVo> lockedResult, List<WareOrderTaskDetailEntity> wareOrderTaskDetailEntities, WareOrderTaskEntity wareOrderTaskEntity) {
        if (!CollectionUtil.isEmpty(lockedResult)) {
            // 库存锁定成功，将锁定得结果添加到mq中，目的是为了未支付的订单自动解锁库存
            List<Long> wareOrderDetailIds = wareOrderTaskDetailEntities.stream().map(WareOrderTaskDetailEntity::getId).collect(Collectors.toList());
            StockLockedTo stockLockedTo = new StockLockedTo();
            stockLockedTo.setId(wareOrderTaskEntity.getId());
            stockLockedTo.setOrderTaskDetailIds(wareOrderDetailIds);
            rabbitTemplate.convertAndSend(WareMqConstant.STOCK_EVENT_EXCHANGE, WareMqConstant.STOCK_LOCKED, stockLockedTo);
        }
    }

    private WareOrderTaskDetailEntity generateOrderWareTaskDetail(OrderItemVo lock, Long orderTaskId, Long wareSkuId) {
        WareOrderTaskDetailEntity taskDetailEntity = WareOrderTaskDetailEntity.builder()
                .skuId(lock.getSkuId())
                .skuName(lock.getTitle())
                .skuNum(lock.getCount())
                .taskId(orderTaskId)
                .wareId(wareSkuId)
                .lockStatus(1)
                .build();

        return taskDetailEntity;
    }

    /**
     * 锁库存，应该和查库存为一个原子操作,采用乐观锁优化(检查只要不超过)
     */
    private Boolean lockWareStock(WareSkuEntity wareSkuEntity, Integer count) {
        return this.baseMapper.lockStock(wareSkuEntity.getId(), count);
    }

    /**
     * 查找满足库存条件的一个仓库（可优化方向，根据用户信息来查询最近的仓库）
     */
    private WareSkuEntity findWareBySkuStockCondition(Long skuId, Integer num) {
        // 条件 skuId = sku_id and num <= stock-stock_locked

        return this.baseMapper.findWareBySkuStockCondition(skuId, num);
    }

    private <T> void buildQuery(Number key, QueryWrapper<T> query, String keyField) {
        if (!(key == null) && !key.toString().equals(ProductConstant.ALL_LIST_ID.toString())) {
            query.eq(keyField, key);
        }
    }

    private <T> void buildKeyQuery(String key, QueryWrapper<T> query, String idField, String nameField) {
        if (!StrUtil.isBlank(key)) {
            query.and(queryWrapper -> queryWrapper.eq(idField, key)
                    .or()
                    .like(nameField, key));
        }
    }

}