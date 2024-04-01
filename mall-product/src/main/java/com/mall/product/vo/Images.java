/**
 * Copyright 2019 bejson.com
 */
package com.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auto-generated: 2019-11-26 10:50:34
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Images {

    private String imgUrl;
    private int defaultImg;

    public Images(String imgUrl) {
        this.imgUrl = imgUrl;
    }


}