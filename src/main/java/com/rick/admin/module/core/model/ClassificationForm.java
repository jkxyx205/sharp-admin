package com.rick.admin.module.core.model;

import com.rick.admin.module.core.entity.Classification;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 表单
 * @author Rick
 * @createdAt 2022-06-29 14:23:00
 */
@Data
@AllArgsConstructor
public class ClassificationForm {

    private Classification classification;

    private List<CharacteristicDTO> characteristicDTOList;
}
