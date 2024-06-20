package com.mall.search.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.mall.common.es.SkuEsModel;
import com.mall.search.constant.ESContant;
import com.mall.search.service.MallSearchService;
import com.mall.search.vo.SearchParamVO;
import com.mall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    private RestHighLevelClient restHighLevelClient;
    private NestedAggregationBuilder nestedAggregationBuilder;

    @Override
    public SearchResult searchByParam(SearchParamVO searchParam) {
        SearchRequest request = buildSearchRequest(searchParam);

        try {
            SearchResponse response = restHighLevelClient.search(request, ESContant.COMMON_OPTIONS);
            return parseToSearchResult(searchParam, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建商品查询请求
     */
    private SearchRequest buildSearchRequest(SearchParamVO searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建查询条件
        buildQueryRequest(searchParam, searchSourceBuilder);
        // 构建高亮条件
        buildHighlightRequest(searchParam, searchSourceBuilder);
        // 构建聚合条件
        buildAggregationRequest(searchParam, searchSourceBuilder);
        // 构建分页条件
        buildPageRequest(searchParam, searchSourceBuilder);
        // 构建排序条件
        buildSortRequest(searchParam, searchSourceBuilder);

        return new SearchRequest(new String[]{ESContant.PRODUCT_INDEX}, searchSourceBuilder);
    }


    private void buildQueryRequest(SearchParamVO searchParam, SearchSourceBuilder searchSourceBuilder) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // must, skuTitle
        String keyword = searchParam.getKeyword();
        if (!StrUtil.isBlank(keyword)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", keyword));
        }
        // filter, brandId multi
        List<Long> brandIdList = searchParam.getBrandId();
        if (!CollectionUtil.isEmpty(brandIdList)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandIdList));
        }
        // filter, catalog multi
        Long catalog3Id = searchParam.getCatalog3Id();
        if (catalog3Id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", catalog3Id));
        }
        // filter, range  A_B,_B,A_
        String skuPrice = searchParam.getSkuPrice();
        if (!StrUtil.isBlank(skuPrice)) {
            String[] price = skuPrice.split("_");
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            if (price.length == 2) {
                // A_B,_B
                if (!StrUtil.isBlank(price[0])) {
                    rangeQuery.gte(Double.parseDouble(price[0]));
                }
                if (!StrUtil.isBlank(price[1])) {
                    rangeQuery.lte(Double.parseDouble(price[1]));
                }
            } else if (price.length == 1) {
                if (!StrUtil.isBlank(price[0])) {
                    rangeQuery.gte(Double.parseDouble(price[0]));
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        // filter nested  id_value1:value2 &
        List<String> attrs = searchParam.getAttrs();
        if (!CollectionUtil.isEmpty(attrs)) {
            attrs.forEach(attr -> {
                // 属性id_属性值：
                String[] attrInfo = attr.split("_");
                BoolQueryBuilder attrQuery = QueryBuilders.boolQuery();
                if (!StrUtil.isBlank(attrInfo[0])) {
                    attrQuery.filter(QueryBuilders.termQuery("attrs.attrId", attrInfo[0]));
                }
                List<String> attrValues = CollectionUtil.newArrayList(attrInfo[1].split(":"));
                attrValues.forEach(attrValue -> {
                    if (!StrUtil.isBlank(attrValue)) {
                        attrQuery.filter(QueryBuilders.termQuery("attrs.attrValue", attrValue));
                    }

                });

                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", attrQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            });
        }
        // filter hasStock 1,0,默认查询有库存的
        Integer hasStock = searchParam.getHasStock();
        if (hasStock != null)
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", hasStock == 1));
        searchSourceBuilder.query(boolQueryBuilder);
    }

    private void buildHighlightRequest(SearchParamVO searchParam, SearchSourceBuilder searchSourceBuilder) {
        // 表示不需要完全匹配字段
        if (StrUtil.isBlank(searchParam.getKeyword())) {
            return;
        }
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("skuTitle")
                .preTags("<em>")
                .postTags("</em>")
                .requireFieldMatch(false);
        searchSourceBuilder.highlighter(highlightBuilder);

    }

    private void buildAggregationRequest(SearchParamVO searchParam, SearchSourceBuilder searchSourceBuilder) {
        // brandAdd
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg")
                .field("brandId")
                .size(20)
                .subAggregation(AggregationBuilders.terms("brand_name_agg")
                        .field("brandName")
                        .size(1))
                .subAggregation(AggregationBuilders.terms("brand_img_agg")
                        .field("brandImg")
                        .size(1));
        // catalogAdd
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg")
                .field("catalogId")
                .size(20)
                .subAggregation(AggregationBuilders.terms("catalog_name_agg")
                        .field("catalogName")
                        .size(1));
        // attrAgg
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs")
                .subAggregation(AggregationBuilders.terms("attr_id_agg")
                        .field("attrs.attrId")
                        .size(10)
                        .subAggregation(AggregationBuilders.terms("attr_name_agg")
                                .field("attrs.attrName")
                                .size(1))
                        .subAggregation(AggregationBuilders.terms("attr_value_agg")
                                .field("attrs.attrValue")
                                .size(10))
                );
        searchSourceBuilder.aggregation(brandAgg).aggregation(catalogAgg).aggregation(attrAgg);
    }

    private void buildPageRequest(SearchParamVO searchParam, SearchSourceBuilder searchSourceBuilder) {
        Integer pageNum = searchParam.getPageNum();
        pageNum = pageNum == null ? 1 : pageNum;
        searchSourceBuilder.from((pageNum - 1) * ESContant.PRODUCT_PAGESIZE).size(ESContant.PRODUCT_PAGESIZE);
    }

    private void buildSortRequest(SearchParamVO searchParam, SearchSourceBuilder searchSourceBuilder) {
        String sort = searchParam.getSort();
        if (StrUtil.isBlank(sort)) return;
        String[] s = sort.split("_");
        String field = s[0];
        SortOrder order = "desc".equals(s[1]) ? SortOrder.DESC : SortOrder.ASC;
        searchSourceBuilder.sort(field, order);

    }

    /**
     * 查询结果构建
     */
    private SearchResult parseToSearchResult(SearchParamVO param, SearchResponse response) {
        SearchResult result = new SearchResult();

        //1、返回的所有查询到的商品
        SearchHits hits = response.getHits();

        buildProductResult(param, hits, result);

        Aggregations aggregations = response.getAggregations();
        buildAttrsAggResult(aggregations, result);

        buildBrandAggResult(aggregations, result);

        buildCatalogAgg(aggregations, result);
        //===============以上可以从聚合信息中获取====================//
        buildPage(param, result, hits);


        buildBreadcrumbNavigation(param, result);


        return result;
    }

    private static void buildBreadcrumbNavigation(SearchParamVO param, SearchResult result) {
        Map<Long, String> attrIdNameMap = result.getAttrs().stream()
                .collect(Collectors.toMap(SearchResult.AttrVo::getAttrId, SearchResult.AttrVo::getAttrName));
        //6、构建面包屑导航
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                //1、分析每一个attrs传过来的参数值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                navVo.setNavName(attrIdNameMap.get(Integer.valueOf(s[0])));

                //2、取消了这个面包屑以后，我们要跳转到哪个地方，将请求的地址url里面的当前置空
                //拿到所有的查询条件，去掉当前
                String encode = null;
                try {
                    encode = URLEncoder.encode(attr, "UTF-8");
                    encode.replace("+", "%20");  //浏览器对空格的编码和Java不一样，差异化处理
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = param.get_queryString().replace("&attrs=" + attr, "");
                navVo.setLink("http://search.mall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(collect);
        }
    }

    private void buildPage(SearchParamVO param, SearchResult result, SearchHits hits) {
        //5、分页信息-页码
        result.setPageNum(param.getPageNum());
        //5、1分页信息、总记录数
        long totalRecords = hits.getTotalHits().value;
        result.setTotal(totalRecords);

        //5、2分页信息-总页码-计算
        long totalPages = totalRecords / ESContant.PRODUCT_PAGESIZE;
        if (totalRecords % ESContant.PRODUCT_PAGESIZE != 0) {
            totalPages++;
        }
        result.setTotalPages((int) totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
    }

    private void buildCatalogAgg(Aggregations aggregations, SearchResult result) {
        //4、当前商品涉及到的所有分类信息
        //获取到分类的聚合
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        Terms catalogAgg = aggregations.get("catalog_agg");
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            //得到分类名
            Terms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }

        result.setCatalogs(catalogVos);
    }

    private void buildBrandAggResult(Aggregations aggregations, SearchResult result) {
        //3、当前商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        //获取到品牌的聚合
        Terms brandAgg = aggregations.get("brand_agg");
        for (Terms.Bucket brandBucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            //1、得到品牌的id
            long brandId = brandBucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            //2、得到品牌的名字
            Aggregations brandInfoAgg = brandBucket.getAggregations();
            Terms brandNameAgg = brandInfoAgg.get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            //3、得到品牌的图片
            Terms brandImgAgg = brandInfoAgg.get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
    }

    private void buildAttrsAggResult(Aggregations aggregations, SearchResult result) {
        //2、当前商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        //获取属性信息的聚合
        ParsedNested attrsAgg = aggregations.get("attr_agg");
        Terms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket aggBucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1、得到属性的id
            long attrId = aggBucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            //2、得到属性的名字
            Aggregations attrAgg = aggBucket.getAggregations();
            Terms attrNameAgg = attrAgg.get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            //3、得到属性的所有值
            Terms attrValueAgg = attrAgg.get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream()
                    .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                    .collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }

        result.setAttrs(attrVos);
    }

    private void buildProductResult(SearchParamVO param, SearchHits hits, SearchResult result) {
        List<SkuEsModel> esModels = new ArrayList<>();
        //遍历所有商品信息
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);

                //判断是否按关键字检索，若是就显示高亮，否则不显示
                if (!StrUtil.isBlank(param.getKeyword())) {
                    //拿到高亮信息显示标题
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String skuTitleValue = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(skuTitleValue);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);
    }


}
