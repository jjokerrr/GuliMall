package com.mall.ware.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.ware.client.MemberClient;
import com.mall.ware.entity.WareInfoEntity;
import com.mall.ware.mapper.WareInfoMapper;
import com.mall.ware.service.WareInfoService;
import com.mall.ware.vo.FareVo;
import com.mall.ware.vo.MemberAddressVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoMapper, WareInfoEntity> implements WareInfoService {

    @Resource
    private MemberClient memberClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<WareInfoEntity> query) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryWareList(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> query = new QueryWrapper<>();
        String key = (String) params.get("key");
        String wareIdField = "id";
        String wareNameField = "name";
        buildKeyQuery(key, query, wareIdField, wareNameField);
        return queryPage(params, query);
    }

    /**
     * 根据区域id获取运费
     */
    @Override
    public FareVo getFareById(Long addrId) {
        R memberAddrInfo = memberClient.info(addrId);
        MemberAddressVo addrInfoData = memberAddrInfo.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        FareVo fareVo = new FareVo();
        fareVo.setAddress(addrInfoData);
        fareVo.setFare(new BigDecimal(10));
        return fareVo;

    }

    private void buildQuery(Number key, QueryWrapper<WareInfoEntity> query, String keyField) {
        if (!(key == null) && !key.toString().equals(ProductConstant.ALL_LIST_ID.toString())) {
            query.eq(keyField, key);
        }
    }

    private void buildKeyQuery(String key, QueryWrapper<WareInfoEntity> query, String idField, String nameField) {
        if (!StrUtil.isBlank(key)) {
            query.and(queryWrapper -> queryWrapper
                    .eq(idField, key)
                    .or()
                    .like(nameField, key));
        }
    }

}