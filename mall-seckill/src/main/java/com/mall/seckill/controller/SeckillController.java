package com.mall.seckill.controller;

import com.mall.common.utils.R;
import com.mall.seckill.service.SeckillService;
import com.mall.seckill.to.SeckillSkuRedisTo;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "测试 Swagger")
@Controller
public class SeckillController {
    @Resource
    private SeckillService seckillService;

    /**
     * 秒杀下单业务
     */
    @GetMapping("/kill")
    public String sekillSku(@RequestParam("sessionId") Long sessionId
            , @RequestParam("skuId") Long skuId
            , @RequestParam("key") String randomCode
            , @RequestParam("num") Integer num
            , Model model) {
//        MemberEntityVO user = UserHolder.getUser();
        String orderSn = seckillService.seckillKill(sessionId, skuId, randomCode, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }

    @ResponseBody
    @GetMapping("/getCurrentSeckillSkus")
    public R getCurrentSeckillInfo() {
        List<SeckillSkuRedisTo> seckillSkuRedisToList = seckillService.getCurrentSeckillInfo();
        return R.ok().put("data", seckillSkuRedisToList);
    }

    /**
     * 获取商品的秒杀预告
     */
    @ResponseBody
    @GetMapping("/seckill/sku/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo seckillSkuRedisTo = seckillService.getLastSkuSeckillInfo(skuId);
        return R.ok().put("data", seckillSkuRedisTo);
    }
}
