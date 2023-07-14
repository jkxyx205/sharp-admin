package com.rick.admin.module.inventory.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/7/14 16:37
 */
@SpringBootTest
class StockAgeServiceTest {

    @Autowired
    private StockAgeService stockAgeService;

    @Test
    void getAge() {
        Map<String, BigDecimal> map = stockAgeService.getAge(696062598608588800L, 300L);
        System.out.println(map);
    }
}