package com.mall.search.constant;

import org.elasticsearch.client.RequestOptions;

public class ESContant {
    //  RestHighLevelClient的通用RequestOption
    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }
    /**
     * sku数据在ES中的索引库
     */
    public static final String PRODUCT_INDEX = "product";
    /**
     * 商品索引页面大小
     */
    public static final Integer PRODUCT_PAGESIZE = 12;
}
