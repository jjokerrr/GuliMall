package com.mall.ware.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.ware.entity.WareInfoEntity;
import com.mall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 23:53:14
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<WareInfoEntity> query);

    PageUtils queryWareList(Map<String, Object> params);

    FareVo getFareById(Long addrId);
}

