package com.mall.search;

import com.mall.search.service.MallSearchService;
import com.mall.search.vo.SearchParamVO;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;

@SpringBootTest
public class SearchApplicationTests {
    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private MallSearchService mallSearchService;

    @Test
    public void testRestHighLevelClient() {
        System.out.println(restHighLevelClient);
    }

    @Test
    public void testClient(){

    }

    @Test
    public void splitStringTest(){
        String AB = "A_B";
        String A = "A_";
        String B = "_B";
        System.out.println(Arrays.toString(A.split("_")));
        System.out.println(Arrays.toString(B.split("_")));
        System.out.println(Arrays.toString(AB.split("_")));
    }

    @Test
    public void testMallSearch(){
        SearchParamVO searchParamVO = new SearchParamVO();
        searchParamVO.setKeyword("HUAWEI");
        searchParamVO.setBrandId(Arrays.asList(9L,10L));
        searchParamVO.setCatalog3Id(225L);
        searchParamVO.setSkuPrice("0_6000");
        searchParamVO.setHasStock(1);
        String attr1 = "15_海思（Hisilicon）";
        String attr2 = "10_12GB";
        searchParamVO.setAttrs(Arrays.asList(attr1,attr2));
        mallSearchService.searchByParam(searchParamVO);
    }




}
