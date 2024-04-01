package com.mall.coupon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.to.MemberPrice;
import com.mall.common.to.SkuReductionTO;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.coupon.entity.MemberPriceEntity;
import com.mall.coupon.entity.SkuFullReductionEntity;
import com.mall.coupon.entity.SkuLadderEntity;
import com.mall.coupon.mapper.SkuFullReductionMapper;
import com.mall.coupon.service.MemberPriceService;
import com.mall.coupon.service.SkuFullReductionService;
import com.mall.coupon.service.SkuLadderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionMapper, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    private SkuLadderService skuLadderService;

    @Resource
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveReduction(SkuReductionTO skuReductionTO) {
        // 减免价格
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtil.copyProperties(skuReductionTO, skuLadderEntity);
        skuLadderEntity.setSkuId(skuReductionTO.getSkuId());
        skuLadderEntity.setAddOther(skuReductionTO.getCountStatus());
        if (skuLadderEntity.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        // 打折
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtil.copyProperties(skuReductionTO, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuReductionTO.getCountStatus());
        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0.00")) > 0) {
            save(skuFullReductionEntity);

        }
        // 会员价格,只有会员价格大于0的时候才代表有意义
        List<MemberPrice> memberPrices = skuReductionTO.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntityList = memberPrices
                .stream()
                .filter(memberPrice -> memberPrice.getPrice() != null
                        && memberPrice.getPrice().compareTo(new BigDecimal("0.00")) > 0)
                .map(memberPrice -> {
                    MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                    memberPriceEntity.setSkuId(skuReductionTO.getSkuId());
                    memberPriceEntity.setMemberLevelId(memberPrice.getId());
                    memberPriceEntity.setMemberPrice(memberPrice.getPrice());
                    memberPriceEntity.setMemberLevelName(memberPriceEntity.getMemberLevelName());
                    memberPriceEntity.setAddOther(1);
                    return memberPriceEntity;
                }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntityList);
    }

}