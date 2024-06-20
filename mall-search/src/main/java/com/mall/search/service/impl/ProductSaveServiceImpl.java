package com.mall.search.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.mall.common.es.SkuEsModel;
import com.mall.search.constant.ESContant;
import com.mall.search.service.ElasticsearchService;
import com.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Resource
    private ElasticsearchService elasticsearchService;

    /**
     * 将商品上架到ES中
     *
     * @return
     */
    @Override
    public Boolean saveProduct(List<SkuEsModel> skuEsModelList) {
        // 使用桶query方法将全部的内容插入到es中
        String indexName = ESContant.PRODUCT_INDEX;
        BulkRequest request = new BulkRequest();
        skuEsModelList.forEach(skuEsModel -> {
            String skuEsModelString = JSON.toJSONString(skuEsModel);
            request.add(new IndexRequest(indexName).id(String.valueOf(skuEsModel.getSkuId()))
                    .source(skuEsModelString, XContentType.JSON));
        });

        try {
            List<String> failListId = elasticsearchService.bulkInsert(request);
            return Boolean.valueOf(CollectionUtil.isEmpty(failListId));
        } catch (IOException e) {
            log.error("插入商品sku数据失败");
            return Boolean.FALSE;
        }
    }


}
