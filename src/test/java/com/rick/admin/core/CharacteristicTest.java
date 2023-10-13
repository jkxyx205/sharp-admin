package com.rick.admin.core;

import com.rick.admin.module.core.entity.Characteristic;
import com.rick.admin.module.core.model.CharacteristicDTO;
import com.rick.admin.module.core.service.CharacteristicService;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/7/20 16:51
 */
@SpringBootTest
public class CharacteristicTest {

    @Resource
    private CharacteristicService characteristicService;

    @Resource
    private MaterialDAO materialDAO;


    /**
     * 特征值颜色
     */
    @Test
    public void insertOrUpdateCharacteristicColor() {
        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code("COLOR")
                .description("颜色")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(Arrays.asList(
                        new CpnConfigurer.CpnOption("亚黑"),
                        new CpnConfigurer.CpnOption("光亮"),
                        new CpnConfigurer.CpnOption("亮黑"),
                        new CpnConfigurer.CpnOption("黑光亮"),
                        new CpnConfigurer.CpnOption("银色"),
                        new CpnConfigurer.CpnOption("白色"),
                        new CpnConfigurer.CpnOption("蓝色"),
                        new CpnConfigurer.CpnOption("黄色"),
                        new CpnConfigurer.CpnOption("橙色"),
                        new CpnConfigurer.CpnOption("橘黄色"),
                        new CpnConfigurer.CpnOption("柠檬黄色"),
                        new CpnConfigurer.CpnOption("法拉利红")
                ))
                .required(true)
                .build());
    }

    @Test
    public void insertOrUpdateCharacteristicLine() {
        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code("JIETOU")
                .description("接头")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(Arrays.asList(
                        new CpnConfigurer.CpnOption("公头"),
                        new CpnConfigurer.CpnOption("母头")
                ))
                .required(true)
                .build());

        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code("XIN")
                .description("芯")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(Arrays.asList(
                        new CpnConfigurer.CpnOption("八芯"),
                        new CpnConfigurer.CpnOption("九芯"),
                        new CpnConfigurer.CpnOption("大九芯"),
                        new CpnConfigurer.CpnOption("十芯")
                ))
                .required(true)
                .build());

        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code("AREA")
                .description("平方")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(Arrays.asList(
                        new CpnConfigurer.CpnOption("1.2平方"),
                        new CpnConfigurer.CpnOption("1.5平方"),
                        new CpnConfigurer.CpnOption("2.0平方"),
                        new CpnConfigurer.CpnOption("2.5平方"),
                        new CpnConfigurer.CpnOption("3.0平方")
                ))
                .required(true)
                .build());

        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code("XIANGXIAN")
                .description("相线")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(Arrays.asList(
                        new CpnConfigurer.CpnOption("3C头子"),
                        new CpnConfigurer.CpnOption("普通子弹头"),
                        new CpnConfigurer.CpnOption("Φ5子弹头"),
                        new CpnConfigurer.CpnOption("O型圈"),
                        new CpnConfigurer.CpnOption("MT60端子")
                ))
                .required(false)
                .build());

        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code("HUOERXIAN")
                .description("霍尔线")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(Arrays.asList(
                        new CpnConfigurer.CpnOption("2.8插片"),
                        new CpnConfigurer.CpnOption("SM-5A插针"),
                        new CpnConfigurer.CpnOption("SM-5Y插簧"),
                        new CpnConfigurer.CpnOption("SM-6A插针"),
                        new CpnConfigurer.CpnOption("SM-6Y插簧")
                ))
                .required(false)
                .build());

        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.NUMBER)
                .code("LENGTH")
                .description("线长mm")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .required(true)
                .build());

        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.NUMBER)
                .code("BP_LENGTH")
                .description("剥皮mm")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .required(false)
                .build());

        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.NUMBER)
                .code("ZX_LENGTH")
                .description("沾锡mm")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .required(false)
                .build());

        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code("LINE_BRAND")
                .description("供应商")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(Arrays.asList(
                        new CpnConfigurer.CpnOption("徐州东庚电器有限公司"),
                        new CpnConfigurer.CpnOption("苏州市威峰塑料有限公司"),
                        new CpnConfigurer.CpnOption("天津君磊科技有限公司"),
                        new CpnConfigurer.CpnOption("无锡联为科技有限公司"),
                        new CpnConfigurer.CpnOption("无锡互天电子科技有限公司"),
                        new CpnConfigurer.CpnOption("天津市海固科技有限公司")
                ))
                .required(false)
                .build());
    }

    /**
     * 特征值: 离合器
     */
    @Test
    public void insertOrUpdateCharacteristic() {
//       insertOrUpdateCharacteristic("LHQ", "离合器", 719895812674441216L);

//        insertOrUpdateCharacteristicWithName("NLCL", "尼龙齿轮", 725412446512111616L);
//        insertOrUpdateCharacteristic("ZHC", "轴承", 719895465524482048L);
//        insertOrUpdateCharacteristic("ZHC2", "轴承", 719895465524482048L);
//        insertOrUpdateCharacteristic("CIGANG", "磁钢", 719895589763960832L);

//        insertOrUpdateCharacteristic("ZHZ", "转子", 719895650870775808L);

//        insertOrUpdateCharacteristicWithName("DG", "端盖", 719895400634404864L);

//        insertOrUpdateCharacteristicWithName("NCQ", "内齿圈", 719895746853228544L);
//        insertOrUpdateCharacteristicWithName("FTL", "辐条轮毂", 719895198196322304L);
//        insertOrUpdateCharacteristicWithName("YTL", "一体轮毂", 719895241229881344L);

        insertOrUpdateCharacteristic("TIEX", "铁芯", 720624056151330816L);

        //        insertOrUpdateCharacteristicWithName("TONGX", "铜线", 725412632437219328L);


//        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
//                .type(Characteristic.CharacteristicTypeEnum.NUMBER)
//                .code("TONGX")
//                .description("铜线KG")
//                .cpnType(CpnTypeEnum.RADIO) // 单选
//                .required(false)
//                .build());
    }

    private void insertOrUpdateCharacteristic(String code, String description, Long categoryId) {
        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code(code)
                .description(description)
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(
                        getSpecificationByCategoryCpnOption(categoryId)
                )
                .required(true)
                .build());
    }

    private  List<CpnConfigurer.CpnOption> getSpecificationByCategoryCpnOption(Long categoryId) {
        List<String> specificationList = materialDAO.getSpecificationByCategory(categoryId);
        return specificationList.stream()
                .map(specification -> new CpnConfigurer.CpnOption(specification.substring(specification.indexOf(",\"") + 2, specification.lastIndexOf("]") - 2))).collect(Collectors.toList());
    }

    private void insertOrUpdateCharacteristicWithName(String code, String description, Long categoryId) {
        insertOrUpdateCharacteristicWithName(code, description, getNameSpecificationByCategoryCpnOption(categoryId));
    }

    private void insertOrUpdateCharacteristicWithName(String code, String description, List<CpnConfigurer.CpnOption> options) {
        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code(code)
                .description(description)
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(options)
                .required(true)
                .build());
    }

    private  List<CpnConfigurer.CpnOption> getNameSpecificationByCategoryCpnOption(Long categoryId) {
        List<Map> specificationList = materialDAO.getNameAndSpecificationByCategory(categoryId);

        return specificationList.stream()
                .map(map -> new CpnConfigurer.CpnOption(map.get("name") + ( Objects.toString(map.get("specification"), "").length() <= 2 ? "" : ((String) map.get("specification")).substring(((String) map.get("specification")).indexOf(",\"") + 2, ((String) map.get("specification")).lastIndexOf("]") - 2) ))).collect(Collectors.toList());
    }

}
