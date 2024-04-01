package com.mall.ware.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.ware.entity.WareInfoEntity;
import com.mall.ware.mapper.WareInfoMapper;
import com.mall.ware.service.WareInfoService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoMapper, WareInfoEntity> implements WareInfoService {

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