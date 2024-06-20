package com.mall.search.controller;

import cn.hutool.core.util.BooleanUtil;
import com.mall.common.es.SkuEsModel;
import com.mall.common.excption.ExceptionCode;
import com.mall.common.utils.R;
import com.mall.search.service.ProductSaveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("search/save")
public class EsSaveController {
    @Resource
    private ProductSaveService productSaveService;

    /**
     * 上架商品到ES中
     */
    @PostMapping("/product")
    public R saveProductStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        Boolean b = productSaveService.saveProduct(skuEsModelList);
        if (BooleanUtil.isFalse(b))
            return R.error(ExceptionCode.PRODUCT_UP_EXCEPTION_CODE.getCode(), ExceptionCode.PRODUCT_UP_EXCEPTION_CODE.getMsg());
        return R.ok();
    }
}
