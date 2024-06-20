package com.mall.product.index;

import cn.hutool.core.collection.CollectionUtil;
import com.mall.common.constant.ProductConstant;
import com.mall.common.constant.RedisConstant;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryService;
import com.mall.product.service.SkuInfoService;
import com.mall.product.utils.RedisUtil;
import com.mall.product.vo.SkuItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private SkuInfoService skuInfoService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 首页静态资源
     */
    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        List<CategoryEntity> categoryEntityList = getCategoryEntities();
        model.addAttribute("categories", categoryEntityList);
        return "index";
    }

    private List<CategoryEntity> getCategoryEntities() {
        List cateFlist = redisUtil.getBeanFromRedis(RedisConstant.PRODUCT_CATEGORY_FLIST, List.class);
        if (!CollectionUtil.isEmpty(cateFlist)) {
            return cateFlist;
        }
        // 获取一级分类数据
        List<CategoryEntity> categoryEntityList = categoryService.getCatalogByPLevel(0, ProductConstant.CATELOG_SHOW_STATUS);
        // 存储到redis中
        redisUtil.saveBeanToRedis(RedisConstant.PRODUCT_CATEGORY_FLIST, categoryEntityList, -1);
        return categoryEntityList;
    }

    //查找全部的分类树
    @GetMapping(value = "/product/catalog.json")
    @ResponseBody
    public Map<String, List<CategoryEntity>> getCatalogJson() {

        Map catalogMap = redisUtil.getBeanFromRedis(RedisConstant.PRODUCT_CATEGORY_INDEX, Map.class);
        if (catalogMap != null) {
            log.info("命中Redis");
            return catalogMap;
        }
        /*String catalogJson = redisTemplate.opsForValue().get(RedisConstant.PRODUCT_CATEGORY_INDEX);
        if (!StrUtil.isBlank(catalogJson)) {
            log.info("命中Redis");
            return JSON.parseObject(catalogJson, Map.class);
        }*/
        List<CategoryEntity> categoryEntities = categoryService.listCategoryTree();
        Map<String, List<CategoryEntity>> catalogJsonMap = categoryEntities.stream()
                .collect(Collectors.toMap(category -> category.getCatId().toString(), CategoryEntity::getChildCategory));
        /*catalogJson = JSON.toJSONString(catalogJsonMap);
        redisTemplate.opsForValue().set(RedisConstant.PRODUCT_CATEGORY_INDEX, catalogJson);*/
        redisUtil.saveBeanToRedis(RedisConstant.PRODUCT_CATEGORY_INDEX, catalogJsonMap, -1);
        return catalogJsonMap;

    }


    @GetMapping("{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = skuInfoService.getItem(skuId);
        model.addAttribute("item", skuItemVO);
        return "item";
    }
}
