package com.rick.admin.module;

import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.common.util.JsonUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author Rick.Xu
 * @date 2023/6/12 14:19
 */
@SpringBootTest
public class BomTemplateTest {

    @Resource
    private EntityCodeDAO<BomTemplate, Long> bomTemplateDAO;


    @Test
    public void testInsertBom() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
//                .id(715257255150227456L)
                .code("DEFAULT2")
                .description("默认")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("屏幕组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                        .typeInstanceId(715220846452412416L)
                                        .placeholder("选择屏幕")
                                        .quantity(BigDecimal.valueOf(1))
                                        .build()))
                                .build(),
                        BomTemplate.Component.builder()
                                .description("主板组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(
                                        Arrays.asList(
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(715220886763868160L)
                                                        .placeholder("选择主板")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(715220915268358144L)
                                                        .placeholder("选择硬盘")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(715220967340642304L)
                                                        .placeholder("选择内存")
                                                        .quantity(BigDecimal.valueOf(2))
                                                        .build()
                                        ))
                                .build()
                ))
                .build());
    }

    @Test
    public void selectByCode() {
        BomTemplate bomTemplate = bomTemplateDAO.selectByCode("DEFAULT").get();
        System.out.println(JsonUtils.toJson(bomTemplate));
    }
}
