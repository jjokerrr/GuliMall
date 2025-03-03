package com.mall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.mall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrVO {
    /**
     * 分组id
     */
    @TableId
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catalogId;
    /**
     * 属性组下全部属性
     */
    List<AttrEntity> attrs;
}
