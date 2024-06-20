package com.mall.product;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.mall.product.client.WareClient;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.entity.SkuImagesEntity;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.index.IndexController;
import com.mall.product.service.*;
import com.mall.product.service.impl.SkuInfoServiceImpl;
import com.mall.product.utils.RedisUtil;
import com.mall.product.vo.SkuItemSaleAttrVO;
import com.mall.product.vo.SpuItemAttrGroupVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
class ProductApplicationTests {

    @Resource
    private SpuInfoService spuInfoService;
    @Resource
    private CategoryService categoryService;
    @Autowired
    private ApplicationContext applicationContext;

    @Resource
    private WareClient wareClient;

    @Autowired
    private IndexController indexController;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    void testInterface() {
        /*
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName("华为");
        categoryService.save(categoryEntity);
        CategoryEntity one = categoryService.query().ge("name", "华为").one();
        System.out.printf(String.valueOf(one));
        */

    }

    @Test
    void testCommonStarter() {
        MybatisPlusInterceptor bean = applicationContext.getBean(MybatisPlusInterceptor.class);
        System.out.println(bean);
    }

    @Test
    public void TestWareSkuStock() {

        System.out.println(wareClient.queryStockBySkuIds(Collections.singletonList(1L)));
    }

    @Test
    public void testup() {
        spuInfoService.up(11L);
    }

    @Test
    public void testRedisTOMap() {
        Map<String, List<CategoryEntity>> catalogJson = indexController.getCatalogJson();
        System.out.println(catalogJson);
    }

    @Test
    public void testRedisUtil() {
        String s = "hello world";
        redisUtil.saveBeanToRedis("testString", s, 10);

        String testString = redisUtil.getBeanFromRedis("testString", String.class);
        System.out.println(StrUtil.isBlank(testString));
    }

    @Test
    public void changDefaultImg() {
        List<SkuInfoEntity> skuInfoEntityList = skuInfoService.list();
        List<SkuInfoEntity> collect = skuInfoEntityList.stream().peek(skuInfoEntity -> {
            SkuImagesEntity one = skuImagesService.query().eq("sku_id", skuInfoEntity.getSkuId()).eq("default_img", 1).one();
            if (one != null)
                skuInfoEntity.setSkuDefaultImg(one.getImgUrl());
        }).collect(Collectors.toList());
        skuInfoService.updateBatchById(collect);
    }

    @Test
    public void testSpuItem() {
        List<SpuItemAttrGroupVO> spuItemAttrGroupVOS =
                ((SkuInfoServiceImpl) skuInfoService).buildSpuAttrGroup(13L,225L);
        return;
    }

    @Test
    public void testSkuAttr(){
        List<SkuItemSaleAttrVO> skuItemSaleAttrVOS = skuSaleAttrValueService.querySaleAttrCombinationBySkuList(Arrays.asList(1, 2, 3));
        System.out.println(skuItemSaleAttrVOS);
    }




}


