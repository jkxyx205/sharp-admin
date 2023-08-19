package com.rick.admin.module.material.service;

import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.admin.module.material.entity.MaterialProfile;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.util.OptionalUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/19 11:03
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class MaterialProfileService {

    EntityDAO<MaterialProfile, Long> materialProfileDAO;

    public Optional<MaterialProfile> getMaterialProfile(@NonNull Long materialId, Long batchId) {
        return OptionalUtils.expectedAsOptional(materialProfileDAO.selectByParams(MaterialProfile.builder()
                        .materialId(materialId)
                        .batchId(batchId)
                .build(), Objects.isNull(batchId) ? "material_id = :materialId AND category = 'MATERIAL'" : "material_id = :materialId AND batch_id = :batchId AND category = 'BATCH'"));
    }

    public String getCharacteristicText(@NonNull Long materialId, Long batchId) {
        Optional<MaterialProfile> materialProfileOptional = getMaterialProfile(materialId, batchId);
        return materialProfileOptional.isPresent() ? materialProfileOptional.get().getCharacteristicValueList().stream().map(CharacteristicValue::getValue).collect(Collectors.joining(" ")) : "";
    }

}