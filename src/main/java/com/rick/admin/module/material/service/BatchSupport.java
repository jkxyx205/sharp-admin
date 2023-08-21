package com.rick.admin.module.material.service;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/19 05:11
 */
@UtilityClass
public class BatchSupport {

    public static String characteristicToCode(List<String> characteristicList) {
        if (CollectionUtils.isEmpty(characteristicList)) {
            return "";
        }

        String string = characteristicList.stream().collect(Collectors.joining("#"));

        StringBuilder unicodeBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char nextChar = string.charAt(i);
            unicodeBuilder.append("u");
            unicodeBuilder.append(Integer.toHexString(nextChar));
        }
        return unicodeBuilder.toString();
    }
}
