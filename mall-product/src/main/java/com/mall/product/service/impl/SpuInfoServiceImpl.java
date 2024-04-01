package com.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.ProductConstant;
import com.mall.common.to.SkuReductionTO;
import com.mall.common.to.SpuBoundTO;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.product.entity.*;
import com.mall.product.client.CouponClient;
import com.mall.product.mapper.SpuInfoMapper;
import com.mall.product.service.*;
import com.mall.product.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<SpuInfoEntity> query) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                query
        );

        return new PageUtils(page);
    }

    // TODO: 由于调用了其他微服务的内容，因此此处应该写分布式事务内容
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

        // 保存sku基本属性 sku_info sku_image sku_sale_attr
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

    private void buildSpuQuery(SpuListVO spuListVO, QueryWrapper<SpuInfoEntity> query) {
        if (BeanUtil.isEmpty(spuListVO)) {
            return;
        }
        String catelogIdField = "catelog_id";
        String brandIdField = "brand_id";
        String statusField = "publish_status";

        Long brandId = spuListVO.getBrandId();
        Integer status = spuListVO.getStatus();
        Long catelogId = spuListVO.getCatelogId();


        // 构造catelogId的查询
        buildQuery(catelogId, query, catelogIdField);
        // brandId
        buildQuery(brandId, query, brandIdField);
        // status
        if (!(status == null)) {
            query.eq(statusField, status);
        }
    }

    private void buildQuery(Number key, QueryWrapper<SpuInfoEntity> query, String keyField) {
        if (!(key == null) && !key.toString().equals(ProductConstant.ALL_LIST_ID.toString())) {
            query.eq(keyField, key);
        }
    }

    private void buildKeyQuery(String key, QueryWrapper<SpuInfoEntity> query, String spuIdField, String spuNameFiled) {
        if (!StrUtil.isBlank(key)) {
            query.and(queryWrapper -> queryWrapper.eq(spuIdField, key)
                    .or()
                    .like(spuNameFiled, key));

        }
    }

    private void saveSkuInfoImageSaleAttrReduction(List<Sku> skus, Long spuId, SpuInfoEntity spuInfoEntity) {
        // 保存sku基本信息

        List<SkuInfoEntity> skuInfoList = skus.stream().map(sku -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtil.copyProperties(sku, skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
            skuInfoEntity.setCatelogId(spuInfoEntity.getCatelogId());
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