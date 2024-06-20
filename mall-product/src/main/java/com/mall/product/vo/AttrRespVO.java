package com.mall.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AttrRespVO extends AttrVO {
    /**
     * 所属分类名称
     */
    private String catalogName;

    /**
     * 所属分组名称
     */
    private String groupName;

    /**
     * 分类完整路径
     */
    private List<Long> catalogPath;

}
