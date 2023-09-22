package com.rick.admin.module.material.service;

import com.rick.admin.module.material.entity.Classification;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/19 05:11
 */
@UtilityClass
public class BatchSupport {

    public static String characteristicToCode(List<Classification> classificationList) {
        if (CollectionUtils.isEmpty(classificationList)) {
            return "";
        }

        List<String> valueList = classificationList.stream().flatMap(p -> {
                    p.getCharacteristicValueList().forEach(characteristicValue -> characteristicValue.setClassificationCode(p.getClassificationCode()));
                    return p.getCharacteristicValueList().stream();
                })
                .filter(characteristicValue -> Objects.nonNull(characteristicValue.getValue()))
                .map(characteristicValue -> characteristicValue.getClassificationCode() + ":" + characteristicValue.getCharacteristicCode() + ":" + characteristicValue.getValue())
                .sorted()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(valueList)) {
            return "";
        }

        String valueString = valueList.stream().collect(Collectors.joining("#"));
        return DigestUtils
                .md5Hex(valueString);
    }

}
