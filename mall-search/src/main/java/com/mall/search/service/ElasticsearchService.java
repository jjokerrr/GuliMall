package com.mall.search.service;

import org.elasticsearch.action.bulk.BulkRequest;

import java.io.IOException;
import java.util.List;

public interface ElasticsearchService {
    List<String> bulkInsert(BulkRequest request) throws IOException;

//    boolean bulkInsert(Collection<? extends Serializable> collection, String indexName);
//
//    boolean bulkInsertWithRetry(Collection<? extends Serializable> collection, String indexName, Integer retry);
}
