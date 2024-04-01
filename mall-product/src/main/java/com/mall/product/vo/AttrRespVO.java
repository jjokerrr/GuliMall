package com.mall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class AttrRespVO extends AttrVO {
    /**
     * 所属分类名称
     */
    private String catelogName;

    /**
     * 所属分组名称
     */
    private String groupName;

    /**
     * 分类完整路径
     */
    private List<Long> catelogPath;

}
