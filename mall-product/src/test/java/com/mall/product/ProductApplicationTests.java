package com.mall.product;

import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ProductApplicationTests {
    @Resource
    private CategoryService categoryService;

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

}
