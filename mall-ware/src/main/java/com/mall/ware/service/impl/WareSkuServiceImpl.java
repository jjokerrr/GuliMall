package com.mall.ware.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.ware.client.ProductClient;
import com.mall.ware.entity.WareSkuEntity;
import com.mall.ware.mapper.WareSkuMapper;
import com.mall.ware.service.WareSkuService;
import com.mall.ware.vo.WareSkuVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {


    @Resource
    private ProductClient productClient;

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
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
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