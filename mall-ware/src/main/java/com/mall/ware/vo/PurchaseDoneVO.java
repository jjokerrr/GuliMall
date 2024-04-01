package com.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVO {
    /**
     * 采购单id
     */
    @NotNull
    private Long id;

    private List<PurchaseItemVO> items;
}
