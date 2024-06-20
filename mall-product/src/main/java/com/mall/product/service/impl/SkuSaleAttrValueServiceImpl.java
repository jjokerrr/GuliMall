package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.entity.SkuSaleAttrValueEntity;
import com.mall.product.mapper.SkuSaleAttrValueMapper;
import com.mall.product.service.SkuSaleAttrValueService;
import com.mall.product.vo.AttrValueWithSkuIdVO;
import com.mall.product.vo.SkuItemSaleAttrVO;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueMapper, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询全部sku的销售属性组合
     */
    @Override
    public List<SkuItemSaleAttrVO> querySaleAttrCombinationBySkuList(Collection<? extends Serializable> skuLists) {

        // 根据全部的skuId信息查询全部的销售属性，根据attr_id进行聚合
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = lambdaQuery()
                .in(SkuSaleAttrValueEntity::getSkuId, skuLists)
                .list();
        // 获取sku集合中全部的属性集合
        Set<SkuItemSaleAttrVO> attrIdName = skuSaleAttrValueEntityList.stream()
                .map((skuSaleAttrValue) -> {
                    SkuItemSaleAttrVO skuItemSaleAttrVO = new SkuItemSaleAttrVO();
                    skuItemSaleAttrVO.setAttrId(skuSaleAttrValue.getAttrId());
                    skuItemSaleAttrVO.setAttrName(skuSaleAttrValue.getAttrName());
                    return skuItemSaleAttrVO;
                })
                .collect(Collectors.toSet());
        // 创建属性值和skuId的map
        Map<String, String> attrIdValueSkuMap = buildAttrValueSkuIdsMap(skuSaleAttrValueEntityList);


        Map<Long, Set<AttrValueWithSkuIdVO>> idValueMap = skuSaleAttrValueEntityList.stream()
                .collect(Collectors
                        .groupingBy(SkuSaleAttrValueEntity::getAttrId, Collectors.mapping((groupRes) -> {
                            AttrValueWithSkuIdVO attrValueWithSkuIdVO = new AttrValueWithSkuIdVO();
                            attrValueWithSkuIdVO.setAttrValue(groupRes.getAttrValue());
                            attrValueWithSkuIdVO.setSkuIds(attrIdValueSkuMap.get(groupRes.getAttrId() + "-" + groupRes.getAttrValue()));
                            return attrValueWithSkuIdVO;
                        }, Collectors.toSet())));
        return attrIdName.stream().peek(skuItemSaleAttrVO -> {

            skuItemSaleAttrVO.setAttrValues(new ArrayList<>(idValueMap.get(skuItemSaleAttrVO.getAttrId())));
        }).collect(Collectors.toList());
    }

    /**
     * 查询sku对应的全部销售属性
     */
    @Override
    public List<String> getSaleAttrsBySkuId(Long skuId) {
        return this.lambdaQuery()
                .eq(SkuSaleAttrValueEntity::getSkuId, skuId)
                .select(SkuSaleAttrValueEntity::getAttrValue)
                .list()
                .stream()
                .map(SkuSaleAttrValueEntity::getAttrValue)
                .collect(Collectors.toList());
    }

    private Map<String, String> buildAttrValueSkuIdsMap(List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList) {
        // 创建一个Map用于保存属性ID和对应的skuId集合
        Map<String, String> attrIdValueSkuMap = new HashMap<>();

        // 根据属性ID和属性值进行聚合
        Map<Long, Map<String, Set<Long>>> attrIdValueSkuGrouped = skuSaleAttrValueEntityList.stream()
                .collect(Collectors.groupingBy(SkuSaleAttrValueEntity::getAttrId,
                        Collectors.groupingBy(SkuSaleAttrValueEntity::getAttrValue,
                                Collectors.mapping(SkuSaleAttrValueEntity::getSkuId, Collectors.toSet()))));

        // 将聚合结果转换为所需的Map结构
        attrIdValueSkuGrouped.forEach((attrId, valueSkuMap) -> {
            valueSkuMap.forEach((attrValue, skuIds) -> {
                String key = attrId + "-" + attrValue;
                String skuIdsString = skuIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                attrIdValueSkuMap.put(key, skuIdsString);
            });
        });
        return attrIdValueSkuMap;
    }

}