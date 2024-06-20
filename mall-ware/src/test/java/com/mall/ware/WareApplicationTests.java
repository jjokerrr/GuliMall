package com.mall.ware;

import com.mall.common.to.WareSkuStockTO;
import com.mall.ware.entity.WareSkuEntity;
import com.mall.ware.mapper.WareSkuMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class WareApplicationTests {

    @Resource
    private WareSkuMapper wareSkuMapper;


    @Test
    public void testWareSkuStock() {
        List<WareSkuStockTO> wareSkuStockTOList = wareSkuMapper.queryStockBySkuIds(Collections.singletonList(1L));
        System.out.println(wareSkuStockTOList);
    }
    @Test
    public void findUnique(){
        WareSkuEntity wareBySkuStockCondition = wareSkuMapper.findWareBySkuStockCondition(1L, 99);
        System.out.println(wareBySkuStockCondition);
    }

}
