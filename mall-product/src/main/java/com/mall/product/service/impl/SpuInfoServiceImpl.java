package com.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.es.SkuEsModel;
import com.mall.common.to.SkuReductionTO;
import com.mall.common.to.SpuBoundTO;
import com.mall.common.to.WareSkuStockTO;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.product.client.CouponClient;
import com.mall.product.client.SearchClient;
import com.mall.product.client.WareClient;
import com.mall.product.entity.*;
import com.mall.product.mapper.SpuInfoMapper;
import com.mall.product.service.*;
import com.mall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoMapper, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private SpuImagesService spuImagesService;

    @Resource
    private AttrService attrService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private CouponClient couponClient;

    @Resource
    private BrandService brandService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private WareClient wareClient;

    @Resource
    private SearchClient searchClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<SpuInfoEntity> query) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }


    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 保存基本spu信息
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtil.copyProperties(spuSaveVo, spuInfoEntity);
        saveBaseSpuInfo(spuInfoEntity);     // 当保存成功时，会自动注入spuInfoEntity的id字段
        Long spuId = spuInfoEntity.getId();
        // 保存spu描述信息
        saveSpuDesc(spuSaveVo, spuId);

        // 保存spu图片集
        saveSpuImages(spuSaveVo, spuId);

        // 保存spu基本属性（规格参数） product_attr_value
        saveSpuBaseAttr(spuSaveVo, spuId);

        // 保存sku基   本属性 sku_info sku_image sku_sale_attr
        List<Sku> skuList = spuSaveVo.getSkus();
        if (CollectionUtil.isEmpty(skuList))
            skuList = Collections.emptyList();
        saveSkuInfoImageSaleAttrReduction(skuList, spuId, spuInfoEntity);


        // 保存积分信息
        SpuBoundTO spuBoundTO = new SpuBoundTO();
        BeanUtil.copyProperties(spuSaveVo.getBounds(), spuBoundTO);
        spuBoundTO.setSpuId(spuInfoEntity.getId());

        R r = couponClient.saveSpuBound(spuBoundTO);
        if (!r.getCode().equals("0")) {
            log.error("保存优惠信息失败");
            throw new RuntimeException("保存失败");
        }


    }

    @Override
    public PageUtils querySpuList(Map<String, Object> params, SpuListVO spuListVO) {
        String key = (String) params.get("key");
        QueryWrapper<SpuInfoEntity> query = new QueryWrapper<>();
        String spuIdField = "id";
        String spuNameFiled = "spu_name";
        // 构造key query
        buildKeyQuery(key, query, spuIdField, spuNameFiled);

        buildSpuQuery(spuListVO, query);

        return queryPage(params, query);
    }

    /**
     * 按照spu将全部sku上架到es中
     */
    @Override
    public void up(Long spuId) {

        // 查找spu对应的全部sku
        List<SkuInfoEntity> skuList = skuInfoService.querySkuBySpuId(spuId);
        if (CollectionUtil.isEmpty(skuList))
            return;
        // 属性对拷，创建esmodel对象
        List<SkuEsModel> skuEsModelList = getSkuEsModels(skuList);
        // brandname brandImg
        Map<Long, BrandEntity> brandIdMap = getBrandIdMap(skuList);

        // catalogname
        Map<Long, CategoryEntity> cateIdMap = getCatalogIdMap(skuList);

        // attr
        List<SkuEsModel.Attrs> attrsList = getSpuUpAttrs(spuId);

        // hasStock
        // 对于全部的远程调用，都需要使用try-catch来避免由于网络波动造成的超时
        Map<Long, Boolean> skuStockMap;
        try {
            skuStockMap = getSkuStockMap(skuList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 统一赋值
        skuEsModelList.forEach(skuEsModel -> {
            // catalog
            CategoryEntity categoryEntity = cateIdMap.get(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            // brand
            BrandEntity brandEntity = brandIdMap.get(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            // attrs
            skuEsModel.setAttrs(attrsList);
            // stock
            skuEsModel.setHasStock(skuStockMap.get(skuEsModel.getSkuId()));
        });

        // ES
        R r = searchClient.saveProductStatusUp(skuEsModelList);
        if (!r.getCode().equals("0")) {
            log.error("保存到es失败");
            // TODO： 接口幂等性，远程接口如果调用失败，考虑采用重试机制
        }

        // 修改spu状态
        updateSpuStatus(spuId, ProductConstant.SPU_NEW_STATUS, ProductConstant.SPU_UP_STATUS);
        updateSpuStatus(spuId, ProductConstant.SPU_DOWN_STATUS, ProductConstant.SPU_UP_STATUS);

    }

    @Override
    public SpuInfoEntity querySpuBySkuId(Long skuId) {
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        Long spuId = skuInfoEntity.getSpuId();
        return getById(spuId);
    }


    @NotNull
    private List<SkuEsModel.Attrs> getSpuUpAttrs(Long spuId) {
        // 查询spu属性和属性值
        List<SkuEsModel.Attrs> attrsList = productAttrValueService.queryBaseAttrforSpu(spuId)
                .stream()
                .map(
                        productAttrValueEntity -> {
                            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                            BeanUtil.copyProperties(productAttrValueEntity, attrs);
                            return attrs;
                        }
                )
                .collect(Collectors.toList());
        return attrsList;
    }

    private Map<Long, CategoryEntity> getCatalogIdMap(List<SkuInfoEntity> skuList) {
        Set<Long> cateLogIdSet = skuList.stream().map(SkuInfoEntity::getCatalogId).collect(Collectors.toSet());
        return getCategoryInfoMap(cateLogIdSet);
    }

    private Map<Long, BrandEntity> getBrandIdMap(List<SkuInfoEntity> skuList) {
        Set<Long> brandIdSet = skuList.stream().map(SkuInfoEntity::getBrandId).collect(Collectors.toSet());
        // 引入brandId，brandEntity的对象Map
        return getBrandInfoMap(brandIdSet);
    }

    @NotNull
    private static List<SkuEsModel> getSkuEsModels(List<SkuInfoEntity> skuList) {

        return skuList.stream()
                .map(skuInfoEntity -> {
                    SkuEsModel skuEsModel = new SkuEsModel();
                    BeanUtil.copyProperties(skuInfoEntity, skuEsModel);
                    // 额外属性赋值
                    skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
                    skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());
                    // 热度初始设置为0
                    skuEsModel.setHotScore(0L);
                    return skuEsModel;
                }).collect(Collectors.toList());
    }

    @NotNull
    private Map<Long, Boolean> getSkuStockMap(List<SkuInfoEntity> skuList) {
        List<Long> skuIdList = skuList.stream()
                .map(SkuInfoEntity::getSkuId)
                .collect(Collectors.toList());
        List<WareSkuStockTO> wareSkuStockTOS = wareClient.queryStockBySkuIds(skuIdList);
        return wareSkuStockTOS.stream()
                .collect(Collectors.toMap(WareSkuStockTO::getSkuId, (wareSkuStockTO) -> wareSkuStockTO.getStock() > 0));
    }


    /**
     * 更新spu发布状态
     */
    private void updateSpuStatus(Long spuId, Long spuOldStatus, Long spuNewStatus) {
        update().set("publish_status", spuNewStatus)
                .eq("id", spuId)
                .eq("publish_status", spuOldStatus)
                .update();
    }

    // 封装获取品牌信息的方法
    private Map<Long, BrandEntity> getBrandInfoMap(Set<Long> brandIdSet) {
        List<BrandEntity> brandEntities = brandService.listByIds(brandIdSet);
        return brandEntities.stream()
                .collect(Collectors.toMap(BrandEntity::getBrandId, Function.identity()));
    }

    // 封装获取类目信息的方法
    private Map<Long, CategoryEntity> getCategoryInfoMap(Set<Long> cateLogId) {
        List<CategoryEntity> categoryEntities = categoryService.listByIds(cateLogId);
        return categoryEntities.stream()
                .collect(Collectors.toMap(CategoryEntity::getCatId, Function.identity()));
    }

    private void buildSpuQuery(SpuListVO spuListVO, QueryWrapper<SpuInfoEntity> query) {
        if (BeanUtil.isEmpty(spuListVO)) {
            return;
        }
        String catalogIdField = "catalog_id";
        String brandIdField = "brand_id";
        String statusField = "publish_status";

        Long brandId = spuListVO.getBrandId();
        Integer status = spuListVO.getStatus();
        Long catalogId = spuListVO.getCatalogId();


        // 构造catalogId的查询
        buildQuery(catalogId, query, catalogIdField);
        // brandId
        buildQuery(brandId, query, brandIdField);
        // status
        if (!(status == null)) {
            query.eq(statusField, status);
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

    private void saveSkuInfoImageSaleAttrReduction(List<Sku> skus, Long spuId, SpuInfoEntity spuInfoEntity) {
        // 保存sku基本信息

        List<SkuInfoEntity> skuInfoList = skus.stream().map(sku -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtil.copyProperties(sku, skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
            // 保存sku中的图片信息，其中前端可能发送不包含url的图片信息，在处理中需要将其排除掉
            List<SkuImagesEntity> imgLists = sku.getImages()
                    .stream()
                    .filter(img -> !StrUtil.isBlank(img.getImgUrl()))
                    .map(img -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(skuImagesEntity.getId());
                        skuImagesEntity.setImgUrl(img.getImgUrl());
                        if (img.getDefaultImg() == 1) {
                            skuInfoEntity.setSkuDefaultImg(img.getImgUrl());

                        }
                        skuImagesEntity.setDefaultImg(img.getDefaultImg());
                        return skuImagesEntity;
                    }).collect(Collectors.toList());

            skuImagesService.saveBatch(imgLists);

            // 保存sku中的销售属性
            List<SkuSaleAttrValueEntity> attrs = sku.getAttr()
                    .stream()
                    .map(attr -> {
                        SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                        BeanUtil.copyProperties(attr, skuSaleAttrValueEntity);
                        skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                        return skuSaleAttrValueEntity;
                    }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(attrs);

            // 保存sku中的打折属性
            SkuReductionTO skuReductionTO = new SkuReductionTO();
            BeanUtil.copyProperties(sku, skuReductionTO);
            skuReductionTO.setSkuId(skuInfoEntity.getSkuId());
            if (skuReductionTO.getFullPrice().compareTo(new BigDecimal(0)) > 0 || skuReductionTO.getFullCount() > 0) {
                // 只有当满减优惠有意义时才会发起满减请求
                R r = couponClient.saveSkuReduction(skuReductionTO);
                if (!r.getCode().equals("0")) {
                    log.error("保存sku折扣信息失败");
                    throw new RuntimeException("保存sku折扣信息失败");
                }
            }

            return skuInfoEntity;
        }).collect(Collectors.toList());


        skuInfoService.saveBatch(skuInfoList);

    }

    private void saveSpuBaseAttr(SpuSaveVo spuSaveVo, Long spuId) {
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        if (CollectionUtil.isEmpty(baseAttrs))
            baseAttrs = Collections.emptyList();

        List<ProductAttrValueEntity> productAttrValueEntityList = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(spuId);
            productAttrValueEntity.setAttrId(attr.getAttrId());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());

            AttrEntity attrId = attrService.getById(productAttrValueEntity.getAttrId());
            productAttrValueEntity.setAttrName(attrId.getAttrName());

            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntityList);
    }

    private void saveSpuImages(SpuSaveVo spuSaveVo, Long spuId) {
        List<Images> imgList = spuSaveVo.getImages();
        if (CollectionUtil.isEmpty(imgList)) imgList = Collections.emptyList();

        List<SpuImagesEntity> spuImagesEntityList = imgList
                .stream()
                .map(img -> {
                    SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                    spuImagesEntity.setSpuId(spuId);
                    spuImagesEntity.setImgUrl(img.getImgUrl());
                    spuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return spuImagesEntity;
                })
                .collect(Collectors.toList());


        spuImagesService.saveBatch(spuImagesEntityList);
    }

    private void saveSpuDesc(SpuSaveVo spuSaveVo, Long spuId) {
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(String.join(",", spuSaveVo.getDecript()));     // 描述字段已蹄片方式提交，多个描述以，隔离
        spuInfoDescService.save(spuInfoDescEntity);
    }

    private boolean saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
        return true;
    }

}