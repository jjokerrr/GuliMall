package com.mall.thirdparty.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.mall.thirdparty.constant.OssConstant;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OssService {
    @Resource
    private OSSClient ossClient;

    @Resource
    private OssConstant ossConstant;
    public Map<String,Object> getSignature(){
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ;

        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);

            String postSignature = ossClient.calculatePostSignature(postPolicy);


            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessid", ossConstant.getAccessKey());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", ossConstant.getHost());
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            return respMap;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
