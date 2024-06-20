package com.mall.order.index;

import com.alibaba.fastjson.JSON;
import com.mall.common.constant.UrlConstant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;
import com.mall.common.utils.UserHolder;
import com.mall.common.vo.MemberEntityVO;
import com.mall.order.service.OrderService;
import com.mall.order.vo.OrderConfirmVo;
import com.mall.order.vo.OrderSubmitVo;
import com.mall.order.vo.SubmitOrderResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class IndexController {
    @Resource
    private OrderService orderService;

    /**
     * 结算确认页
     */
    @GetMapping(value = "/toTrade")
    public String toTrade(Model model, HttpServletRequest request) {

        MemberEntityVO user = UserHolder.getUser();
        if (user == null) {
            log.info("用户尚未登录");
            return "redirect:" + UrlConstant.LOGIN_URL;
        }

        OrderConfirmVo confirmVo = orderService.confirmOrder();

        model.addAttribute("confirmOrderData", confirmVo);
        //展示订单确认的数据

        return "confirm";
    }

    /**
     * 订单列表页面
     */
    @GetMapping(value = "/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", required = false, defaultValue = "0") Integer pageNum,
                                  Model model, HttpServletRequest request) {
        MemberEntityVO user = UserHolder.getUser();
        Long userId = user.getId();
        //获取到支付宝给我们转来的所有请求数据
        //request,验证签名



        //查出当前登录用户的所有订单列表数据
        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum.toString());
        PageUtils pageUtils = orderService.queryPageWithItem(page, userId);
        R orderInfo = R.ok().put("page", pageUtils);
        // 查询订单服务以获得订单列表
        System.out.println(JSON.toJSONString(orderInfo));
        model.addAttribute("orders",orderInfo);

        return "orderList";
    }

    /**
     * 提交订单,由于是表单方式提交数据，这里不能使用@RequestBody注解声明请求体
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes attributes) {
        SubmitOrderResponseVo orderResponseVo = null;
        try {
            orderResponseVo = orderService.submit(orderSubmitVo);
        } catch (Exception e) {
            attributes.addFlashAttribute("msg",e.getMessage());
            // 出现异常，返回订单详情页
            return "redirect:" + UrlConstant.CONFIRM_ORDER_URL;
        }
        if (orderResponseVo.getCode() != 0) {
            // 提交订单失败，返回提交页面
            return "redirect:" + UrlConstant.CONFIRM_ORDER_URL;
        }
        // 跳转支付页面
        model.addAttribute("submitOrderResp", orderResponseVo);
        return "pay";

    }

}
