package com.mall.member.index;

import com.alibaba.fastjson.JSON;
import com.mall.common.constant.UrlConstant;
import com.mall.common.utils.R;
import com.mall.common.utils.UserHolder;
import com.mall.common.vo.MemberEntityVO;
import com.mall.member.client.OrderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {
    @Resource
    private OrderClient orderClient;

    @GetMapping("/memberOrderList.html")
    public String getMemberOrderList(@RequestParam(value = "pageNum", required = false, defaultValue = "0") Integer pageNum,
                                     Model model) {
        MemberEntityVO user = UserHolder.getUser();
        if (user == null) {
            return "redirect:" + UrlConstant.LOGIN_URL;
        }
        //查出当前登录用户的所有订单列表数据
        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum.toString());
        page.put("userId", user.getId().toString());

        //远程查询订单服务订单数据
        R orderInfo = orderClient.listWithItem(page);
        System.out.println(JSON.toJSONString(orderInfo));
        model.addAttribute("orders", orderInfo);

        return "orderList";
    }
}
