package com.rick.admin.core;

import com.rick.admin.module.core.entity.Characteristic;
import com.rick.admin.module.core.model.CharacteristicDTO;
import com.rick.admin.module.core.service.CharacteristicService;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author Rick.Xu
 * @date 2023/7/20 16:51
 */
@SpringBootTest
public class CharacteristicTest {

    @Resource
    private CharacteristicService characteristicService;

    /**
     * 添加特征值
     */
    @Test
    public void insertOrUpdateCharacteristic() {
        characteristicService.saveOrUpdate(CharacteristicDTO.builder()
                .type(Characteristic.CharacteristicTypeEnum.TEXT)
                .code("COLOR")
                .description("颜色")
                .cpnType(CpnTypeEnum.RADIO) // 单选
                .options(Arrays.asList(
                        new CpnConfigurer.CpnOption("亚黑"),
                        new CpnConfigurer.CpnOption("光亮")
                ))
                .required(true)
                .build());
    }

}
