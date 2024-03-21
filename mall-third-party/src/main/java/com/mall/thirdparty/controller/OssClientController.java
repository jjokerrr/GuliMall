package com.mall.thirdparty.controller;

import com.mall.common.utils.R;
import com.mall.thirdparty.service.OssService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("third-party/oss")
public class OssClientController {
    @Resource
    private OssService ossService;

    @GetMapping("/policy")
    public R getOssSignature() {
        // 签名直传服务
        // 设置上传回调URL，即回调服务器地址，用于处理应用服务器与OSS之间的通信。OSS会在文件上传完成后，把文件上传信息通过此回调URL发送给应用服务器。
//        String callbackUrl = "https://192.168.121.1:8006";
        // 设置上传到OSS文件的前缀，可置空此项。置空后，文件将上传至Bucket的根目录下。
        Map<String, Object> signature = ossService.getSignature();
        return R.ok().put("data",signature);
    }

}
