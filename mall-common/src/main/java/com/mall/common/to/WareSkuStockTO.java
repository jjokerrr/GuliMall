package com.mall.common.to;

import lombok.Data;

@Data
public class WareSkuStockTO {
    /**
     * sku_id
     */
    private Long skuId;

    /**
     * 库存数
     */
    private Integer stock;
}
