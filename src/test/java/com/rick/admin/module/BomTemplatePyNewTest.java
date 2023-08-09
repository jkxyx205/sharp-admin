package com.rick.admin.module;

import com.rick.admin.module.produce.entity.BomTemplate;
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
public class BomTemplatePyNewTest {

    @Resource
    private EntityCodeDAO<BomTemplate, Long> bomTemplateDAO;

    @Test
    public void testInsertBom1() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(719948653090836480L)
                .code("ftl")
                .description("辐条轮组件")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("辐条轮组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(
                                        Arrays.asList(
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(719895198196322304L)
                                                        .placeholder("选择轮毂")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(719895465524482048L)
                                                        .placeholder("选择轴承")
                                                        .quantity(BigDecimal.valueOf(2))
                                                        .build(),
                                                BomTemplate.ComponentDetail.builder()
                                                        .type(BomTemplate.TypeEnum.CATEGORY)
                                                        .typeInstanceId(719895746853228544L)
                                                        .placeholder("选择内齿圈")
                                                        .quantity(BigDecimal.valueOf(1))
                                                        .build()
                                        ))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom2() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(719948522824142848L)
                .code("lhq")
                .description("离合器组件")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("离合器组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895812674441216L)
                                                .placeholder("选择离合器")
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.MATERIAL)
                                                .typeInstanceId(719947044550086656L)
                                                .quantity(BigDecimal.valueOf(3))
                                                .build()))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom3() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(719949713226338304L)
                .code("nlcl")
                .description("尼龙齿轮组件")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("尼龙齿轮组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895465524482048L)
                                                .placeholder("选择轴承")
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895779795292160L)
                                                .placeholder("选择尼龙齿轮")
                                                .quantity(BigDecimal.valueOf(1))
                                                .build()))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom5() {
        // 测速磁钢 和 磁钢的区别
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(720611826450059264L)
                .code("duan_gai")
                .description("端盖组件")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("端盖组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895400634404864L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895465524482048L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895589763960832L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .required(false)
                                                .build()))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom6() {
        // 测速磁钢 和 磁钢的区别
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(720612292575645696L)
                .code("zhuan_zi")
                .description("转子组件")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("转子组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895650870775808L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895465524482048L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895589763960832L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build()))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom7() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(720624597170409472L)
                .code("xianbao")
                .description("线包组件")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("线包组件")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(720624056151330816L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719896181416677376L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build()))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom8() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(720625886516236288L)
                .code("jxbcp")
                .description("机芯半成品")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("机芯半成品")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(720625649131212800L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.MATERIAL)
                                                .typeInstanceId(720620415721951232L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build()))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom9() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(720627424512987136L)
                .code("dingzizu")
                .description("机芯(定子组)")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("机芯(定子组)")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719896181416677376L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.MATERIAL)
                                                .typeInstanceId(720620630394818560L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build()))
                                .build()
                ))
                .build());
    }

    @Test
    public void testInsertBom4() {
        bomTemplateDAO.insertOrUpdate(BomTemplate.builder()
                .id(719951547265454080L)
                .code("pd")
                .description("辐条轮产品")
                .componentList(Arrays.asList(
                        BomTemplate.Component.builder()
                                .description("辐条轮产品")
                                .quantity(BigDecimal.ONE)
                                .unit("TAO")
                                .componentDetailList(Arrays.asList(BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.MATERIAL)
                                                .typeInstanceId(719944505645600768L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.MATERIAL)
                                                .typeInstanceId(719944436586385408L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.MATERIAL)
                                                .typeInstanceId(720618998089142272L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.MATERIAL)
                                                .typeInstanceId(720620280493395968L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.MATERIAL)
                                                .typeInstanceId(720620776251740160L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build(),
                                        BomTemplate.ComponentDetail.builder()
                                                .type(BomTemplate.TypeEnum.CATEGORY)
                                                .typeInstanceId(719895707477102592L)
                                                .quantity(BigDecimal.valueOf(1))
                                                .build()
                                ))
                                .build()
                ))
                .build());
    }
}
