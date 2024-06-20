package com.mall.common.to.mq;

import lombok.Data;

import java.util.List;



@Data
public class StockLockedTo {

    /**
     * 库存工作单的id
     */
    private Long id;

    /**
     * 工作单详情id
     */
    private List<Long> orderTaskDetailIds;
}
