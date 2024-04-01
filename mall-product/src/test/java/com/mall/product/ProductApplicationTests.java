package com.mall.product;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.mall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

@SpringBootTest
class ProductApplicationTests {
    @Resource
    private CategoryService categoryService;
    @Autowired
    private ApplicationContext applicationContext;


    @Test
    void testInterface() {
        /*
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName("华为");
        categoryService.save(categoryEntity);
        CategoryEntity one = categoryService.query().ge("name", "华为").one();
        System.out.printf(String.valueOf(one));
        */

    }

    @Test
    void testCommonStarter(){
        MybatisPlusInterceptor bean = applicationContext.getBean(MybatisPlusInterceptor.class);
        System.out.println(bean);
    }

}


