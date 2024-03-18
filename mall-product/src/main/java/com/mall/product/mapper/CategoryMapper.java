package com.mall.product.mapper;

import com.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zzh
 * @email zzh20001022@163.com
 * @date 2024-03-18 21:45:49
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
	
}
