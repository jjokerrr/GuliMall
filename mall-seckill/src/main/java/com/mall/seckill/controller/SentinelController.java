package com.mall.seckill.controller;

import com.mall.common.utils.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sentinel")
public class SentinelController {
    @GetMapping("getInfo")
    public R sentinelInterface() {
        return R.ok("sentinel的测试接口");
    }

}
