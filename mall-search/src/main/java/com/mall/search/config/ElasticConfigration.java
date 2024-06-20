package com.mall.search.config;

import com.mall.search.properties.ElasticProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticConfigration {



    @Bean
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient(ElasticProperties elasticProperties) {

        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(elasticProperties.getUrl(), elasticProperties.getPort())));

    }


}
