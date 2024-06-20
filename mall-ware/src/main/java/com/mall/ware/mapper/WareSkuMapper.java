package com.mall.ware.mapper;

import com.mall.common.to.WareSkuStockTO;
import com.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:53:14
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {
    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    List<WareSkuStockTO> queryStockBySkuIds(@Param("skuIds") List<Long> skuIds);

    WareSkuEntity findWareBySkuStockCondition(@Param("skuId") Long skuId, @Param("num") Integer num);

    Boolean lockStock(@Param("id") Long id, @Param("count") Integer count);

    boolean releaseStock(@Param("wareId") Long wareId, @Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    void deductStock(@Param("wareId") Long wareId, @Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);
}
