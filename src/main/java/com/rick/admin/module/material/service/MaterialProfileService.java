package com.rick.admin.module.material.service;

import com.rick.admin.module.material.entity.MaterialProfile;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.db.util.OptionalUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.util.*;
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

    SharpService sharpService;

    public Map<String, MaterialProfile> getMaterialProfile(Collection<String> materialIdBatchIdStringCollection) {
        Assert.notEmpty(materialIdBatchIdStringCollection, "不能为空");
        return materialProfileDAO.selectByParams(Params.builder(1).pv("materialIdBatchIdString", materialIdBatchIdStringCollection).build(), "concat(material_id, ifnull(batch_id, '')) IN (:materialIdBatchIdString)")
                .stream().collect(Collectors.toMap(materialProfile -> MaterialProfileSupport.materialIdBatchIdString(materialProfile.getMaterialId(), materialProfile.getBatchId()), materialProfile -> materialProfile));
    }

    public Optional<MaterialProfile> getMaterialProfile(@NonNull Long materialId, Long batchId) {
        return OptionalUtils.expectedAsOptional(materialProfileDAO.selectByParams(MaterialProfile.builder()
                        .materialId(materialId)
                        .batchId(batchId)
                .build(), Objects.isNull(batchId) ? "material_id = :materialId AND category = 'MATERIAL'" : "material_id = :materialId AND batch_id = :batchId AND category = 'BATCH'"));
    }

    public String getCharacteristicText(@NonNull Long materialId, Long batchId) {
        String materialIdBatchIdString = MaterialProfileSupport.materialIdBatchIdString(materialId, batchId);
        return getCharacteristicText(Arrays.asList(materialIdBatchIdString)).get(materialIdBatchIdString);
    }

    /**
     *
     * @param materialIdBatchIdStringCollection  materialIdBatchId
     * @return <materialIdBatchIdString, CharacteristicText>
     */
    public Map<String, String> getCharacteristicText(Collection<String> materialIdBatchIdStringCollection) {
        Assert.notEmpty(materialIdBatchIdStringCollection, "不能为空");
        String sql = "select concat(material_id, ifnull(batch_id, '')) materialIdBatchIdString, group_concat(case when characteristic_code = 'LENGTH' then concat(mm_characteristic_value.value, 'mm') else mm_characteristic_value.value end order by mm_characteristic_value.id asc SEPARATOR ' ') characteristicText from mm_profile left join mm_characteristic_value on mm_profile.id = mm_characteristic_value.reference_id where concat(material_id, ifnull(batch_id, '')) IN (:materialIdBatchIdString)\n" +
                " group by concat(material_id, ifnull(batch_id, ''))";
        return sharpService.queryForKeyValue(sql, Params.builder(1).pv("materialIdBatchIdString", materialIdBatchIdStringCollection).build());
    }

}