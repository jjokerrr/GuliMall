package com.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.product.client.SeckillClient;
import com.mall.product.entity.ProductAttrValueEntity;
import com.mall.product.entity.SkuImagesEntity;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.entity.SpuInfoDescEntity;
import com.mall.product.mapper.SkuInfoMapper;
import com.mall.product.service.*;
import com.mall.product.vo.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private SeckillClient seckillClient;


    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<SkuInfoEntity> query) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils querySkuList(Map<String, Object> params, SkuListVO skuListVO) {
        String key = (String) params.get("key");
        QueryWrapper<SkuInfoEntity> query = new QueryWrapper<>();
        String skuIdField = "sku_id";
        String skuNameFiled = "sku_name";
        // 构造key query
        buildKeyQuery(key, query, skuIdField, skuNameFiled);

        buildSkuQuery(skuListVO, query);

        return queryPage(params, query);
    }

    @Override
    public List<SkuInfoEntity> querySkuBySpuId(Long spuId) {
        return query().eq("spu_id", spuId).list();
    }

    /**
     * sku商品详情,使用异步线程池优化
     */
    @Override
    public SkuItemVO getItem(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = new SkuItemVO();

        // 查找sku基本信息
        CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVO.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, threadPoolExecutor);

        // 查找sku对应的spu规格属性
        CompletableFuture<Void> groupAttrsCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            Long spuId = res.getSpuId();
            Long catalogId = res.getCatalogId();
            List<SpuItemAttrGroupVO> spuItemAttrGroupVOS = buildSpuAttrGroup(spuId, catalogId);
            skuItemVO.setGroupAttrs(spuItemAttrGroupVOS);
        }, threadPoolExecutor);

        // 获取spu的详情介绍信息
        CompletableFuture<Void> descCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            Long spuId = res.getSpuId();
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuId);
            skuItemVO.setDesc(spuInfoDesc);
        }, threadPoolExecutor);

        // 查找spu的全部销售属性
        CompletableFuture<Void> saleAttrCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            Long spuId = res.getSpuId();
            List<Long> skuIdList = querySkuBySpuId(spuId).stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            List<SkuItemSaleAttrVO> saleAttrs = skuSaleAttrValueService.querySaleAttrCombinationBySkuList(skuIdList);
            skuItemVO.setSaleAttr(saleAttrs);
        }, threadPoolExecutor);

        // 查找sku全部图片
        CompletableFuture<Void> skuImgEntityCompletableFuture = CompletableFuture.runAsync(
                () -> {
                    List<SkuImagesEntity> skuImgs = skuImagesService.getImgBySkuId(skuId);
                    skuItemVO.setImages(skuImgs);

                }, threadPoolExecutor);

        // 构建秒杀优惠信息
        CompletableFuture<Void> seckillSkuVOCompletableFuture = CompletableFuture.runAsync(() -> {
            R skuSeckillInfoResponse = seckillClient.getSkuSeckillInfo(skuId);
            if (!skuSeckillInfoResponse.getCode().equals("0")) {
                throw new RuntimeException("查询优惠信息失败");
            }
            SeckillSkuVO data = skuSeckillInfoResponse.getData("data", new TypeReference<SeckillSkuVO>() {
            });
            skuItemVO.setSeckillSkuVo(data);
        }, threadPoolExecutor);


        /*Long spuId = skuInfo.getSpuId();
        skuItemVO.setInfo(skuInfo);


        // 查找sku对应的spu规格属性
        List<SpuItemAttrGroupVO> spuItemAttrGroupVOS = buildSpuAttrGroup(spuId, skuInfo.getCatalogId());
        skuItemVO.setGroupAttrs(spuItemAttrGroupVOS);


        // 获取spu的详情介绍信息
        SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuId);
        skuItemVO.setDesc(spuInfoDesc);

        // 查找spu全部销售属性
        List<Long> skuIdList = querySkuBySpuId(spuId).stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        List<SkuItemSaleAttrVO> saleAttrs = skuSaleAttrValueService.querySaleAttrCombinationBySkuList(skuIdList);
        skuItemVO.setSaleAttr(saleAttrs);*/

        // 等待全部异步任务完成
        CompletableFuture.allOf(groupAttrsCompletableFuture
                , descCompletableFuture
                , saleAttrCompletableFuture
                , skuImgEntityCompletableFuture
                , seckillSkuVOCompletableFuture).get();

        return skuItemVO;
    }

    public List<SpuItemAttrGroupVO> buildSpuAttrGroup(Long spuId, Long catalogId) {
        if (spuId == null) return null;
        // 查找spu对应的属性和属性组
        List<SpuItemAttrGroupVO> spuItemAttrGroupList = attrGroupService.querySpuItemAttrByCatelogId(catalogId);

        // 查找属性，属性值信息
        List<ProductAttrValueEntity> productAttrValues = productAttrValueService.querySpuAttrById(spuId);
        Map<Long, String> attrIdValueMap = productAttrValues.stream()
                .collect(Collectors.toMap(ProductAttrValueEntity::getAttrId, ProductAttrValueEntity::getAttrValue));

        // 对列表中的属性进行赋值
        spuItemAttrGroupList.forEach(spuItemAttrGroupVO -> {
            spuItemAttrGroupVO.getAttrs().forEach(attr -> {
                String attrValue = attrIdValueMap.get(attr.getAttrId());
                attr.setAttrValue(attrValue);
            });
        });
        return spuItemAttrGroupList;
    }


    private void buildSkuQuery(SkuListVO spuListVO, QueryWrapper<SkuInfoEntity> query) {
        if (BeanUtil.isEmpty(spuListVO)) {
            return;
        }
        String catalogIdField = "catalog_id";
        String brandIdField = "brand_id";
        String priceField = "price";

        Long brandId = spuListVO.getBrandId();
        String max = spuListVO.getMax();
        String min = spuListVO.getMin();
        Long catalogId = spuListVO.getCatalogId();


        // 构造catalogId的查询
        buildQuery(catalogId, query, catalogIdField);
        // brandId
        buildQuery(brandId, query, brandIdField);
        // price

        if (!StrUtil.isBlank(max) && new BigDecimal(max).compareTo(BigDecimal.ZERO) > 0) {
            query.le(priceField, max);
        }
        if (!StrUtil.isBlank(min) && new BigDecimal(min).compareTo(BigDecimal.ZERO) > 0) {
            query.ge(priceField, min);
        }
    }

    private <T> void buildQuery(Number key, QueryWrapper<T> query, String keyField) {
        if (!(key == null) && !key.toString().equals(ProductConstant.ALL_LIST_ID.toString())) {
            query.eq(keyField, key);
        }
    }

    private <T> void buildKeyQuery(String key, QueryWrapper<T> query, String idField, String nameField) {
        if (!StrUtil.isBlank(key)) {
            query.and(queryWrapper -> queryWrapper.eq(idField, key)
                    .or()
                    .like(nameField, key));
        }
    }


}