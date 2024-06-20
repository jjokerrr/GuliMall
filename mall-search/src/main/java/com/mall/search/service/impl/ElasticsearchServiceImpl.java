package com.mall.search.service.impl;

import com.mall.search.service.ElasticsearchService;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mall.search.constant.ESContant.COMMON_OPTIONS;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    @Resource
    private RestHighLevelClient restHighLevelClient;




    @Override
    public List<String> bulkInsert(BulkRequest request) throws IOException {
        BulkResponse bulk = restHighLevelClient.bulk(request, COMMON_OPTIONS);
        List<String> failIdList = new ArrayList<>();
        boolean b = bulk.hasFailures();
        for (BulkItemResponse item : bulk.getItems()) {
            if (item.isFailed()) {
                String id = item.getId();
                failIdList.add(id);
            }
        }
        return failIdList;

    }

//    @Override
//    public boolean bulkInsertWithRetry(Collection<? extends Serializable> collection, String indexName, Integer retry) {
//        return false;
//    }
}
