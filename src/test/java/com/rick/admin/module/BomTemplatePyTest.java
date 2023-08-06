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
public class BomTemplatePyTest {

    @Resource
    private EntityCodeDAO<BomTemplate, Long> bomTemplateDAO;

    @Test
    public void testInsertBom1() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
//                .id(717388507961626624L)
                .code("zhuanzi")
                .description("转子组件")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("转子组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(
                                        Arrays.asList(
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717361547617243136L)
                                                        .placeholder("选择转子")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717361597823062016L)
                                                        .placeholder("选择轴承")
                                                        .quantity(BigDecimal.valueOf(2))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717362164590972928L)
                                                        .placeholder("选择磁钢")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build()
                                        ))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom3() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(717388507961626624L)
                .code("DEFAULT")
                .description("默认")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("轮毂组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(717361462728724480L)
                                                .placeholder("选择轮毂")
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(717361597823062016L)
                                                .placeholder("选择轴承")
                                                .quantity(BigDecimal.valueOf(1))
                                                .build()))
                                .build(),
                        BomTemplate.Component.builder()
                                .description("转子组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(
                                        Arrays.asList(
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717361547617243136L)
                                                        .placeholder("选择转子")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717361597823062016L)
                                                        .placeholder("选择轴承")
                                                        .quantity(BigDecimal.valueOf(2))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717362164590972928L)
                                                        .placeholder("选择磁钢")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build()
                                        ))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom4() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
//                .id(717388507961626624L)
                .code("DEFAULT_1")
                .description("test_多bom")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("轮毂组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(717361462728724480L)
                                                .placeholder("选择轮毂")
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(717361597823062016L)
                                                .placeholder("选择轴承")
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.MATERIAL)
                                                .typeInstanceId(719170708285952000L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build()))

                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom2() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(717388507961626624L)
                .code("DEFAULT")
                .description("默认")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("轮毂组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                        .typeInstanceId(717361462728724480L)
                                        .placeholder("选择轮毂")
                                        .quantity(BigDecimal.valueOf(1))
                                        .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(717361597823062016L)
                                                .placeholder("选择轴承")
                                                .quantity(BigDecimal.valueOf(1))
                                                .build()))
                                .build(),
                        BomTemplate.Component.builder()
                                .description("端盖组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(
                                        Arrays.asList(
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717361547617243136L)
                                                        .placeholder("选择端盖")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717361597823062016L)
                                                        .placeholder("选择轴承")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build()
                                        ))
                                .build(),
                        BomTemplate.Component.builder()
                                .description("转子组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(
                                        Arrays.asList(
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717361547617243136L)
                                                        .placeholder("选择转子")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717361597823062016L)
                                                        .placeholder("选择轴承")
                                                        .quantity(BigDecimal.valueOf(2))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717362164590972928L)
                                                        .placeholder("选择磁钢")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build()
                                        ))
                                .build(),
                        BomTemplate.Component.builder()
                                .description("离合器组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(
                                        Arrays.asList(
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717362593890570240L)
                                                        .placeholder("选择离合器")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717362545702211584L)
                                                        .placeholder("选择尼龙齿轮")
                                                        .quantity(BigDecimal.valueOf(3))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(717361597823062016L)
                                                        .placeholder("选择轴承")
                                                        .quantity(BigDecimal.valueOf(3))
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
