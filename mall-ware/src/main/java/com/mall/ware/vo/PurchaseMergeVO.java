package com.mall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseMergeVO {
    /**
     * 采购单id
     */
    public Long purchaseId;

    /**
     * 采购需求id
     */
    public List<Long> items;
}
