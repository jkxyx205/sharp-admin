package com.rick.admin.module.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rick.admin.module.core.model.CharacteristicDTO;
import com.rick.admin.module.core.service.CharacteristicManager;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类
 * @author Rick.Xu
 * @date 2023/7/20 16:03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "core_classification", comment = "分类")
public class Classification extends BaseCodeEntity<Long> {

    String description;

    @JsonIgnore
    @ManyToMany(thirdPartyTable = "core_classification_characteristic", referenceTable = "core_characteristic"
            , columnDefinition="classification_id", referenceColumnName = "characteristic_id")
    private List<Characteristic> characteristicList;

    public List<CharacteristicDTO> getCharacteristicValue() {
        if (CollectionUtils.isNotEmpty(characteristicList)) {
            return characteristicList.stream().map(characteristic -> CharacteristicManager.combine(characteristic)).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}