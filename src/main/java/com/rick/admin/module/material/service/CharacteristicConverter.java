package com.rick.admin.module.material.service;

import com.rick.common.util.JsonUtils;
import com.rick.meta.dict.convert.ValueConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/2 17:21
 */
@Component
public class CharacteristicConverter implements ValueConverter<String> {

    @Override
    public String convert(Object context, String value) {
        // [["颜色","红色"],["大小","小"]]
        if (Objects.isNull(value)) {
            return null;
        }

        List<List> characteristicList = JsonUtils.toList(value, List.class);
        if (CollectionUtils.isEmpty(characteristicList)) {
            return null;
        }
        return characteristicList.stream().map(list -> list.get(1).toString()).collect(Collectors.joining(" "));

    }
}
