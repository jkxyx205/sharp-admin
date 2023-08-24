package com.rick.admin.module.material.service;

import com.rick.admin.module.core.entity.Characteristic;
import com.rick.admin.module.core.service.ClassificationService;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.material.entity.MaterialProfile;
import com.rick.common.util.IdGenerator;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.support.EntityCodeIdFillService;
import com.rick.db.service.support.Params;
import com.rick.db.util.OptionalUtils;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/1 23:46
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class MaterialService {

    MaterialDAO materialDAO;

    final DictService dictService;

    EntityCodeDAO<Characteristic, Long> characteristicDAO;

    EntityDAO<MaterialProfile, Long> materialProfileDAO;

    EntityCodeDAO<com.rick.admin.module.core.entity.Classification, Long> classificationDAO;

    EntityCodeIdFillService entityCodeIdFillService;

    ClassificationService classificationService;

    MaterialProfileService materialProfileService;

    public void saveOrUpdate(Material material) {
        handleClassification(material.getClassificationList());
        handleMaterialProfile(material);

        if (Objects.isNull(material.getId())) {
            materialDAO.insert(material);
        } else {
            materialDAO.update(material);
        }
    }

    public Optional<Material> findByCode(String code) {
        Optional<Material> optional = materialDAO.selectByCode(code);

        if (optional.isPresent()) {
            handleCharacteristic(optional.get());
        }

        return optional;
    }

    public Optional<Material> findById(Long id) {
        Optional<Material> optional = materialDAO.selectById(id);

        if (optional.isPresent()) {
            handleCharacteristic(optional.get());
        }

        return optional;
    }

    public void fillMaterialDescription(Collection<? extends MaterialDescriptionHandler> materialDescriptionList) {
        if (CollectionUtils.isNotEmpty(materialDescriptionList)) {
            consumeMaterialDescription(materialDescriptionList.stream().map(MaterialDescriptionHandler::getMaterialId).collect(Collectors.toSet()), (idMaterialMap, dictService) -> {
                for (MaterialDescriptionHandler handler : materialDescriptionList) {
                    MaterialDescription materialDescription = new MaterialDescription(handler.getMaterialId());
                    handler.setMaterialDescription(materialDescription);

                    Material material = idMaterialMap.get(materialDescription.getId());
                    if (material == null) {
                        continue;
                    }

                    materialDescription.setCode(material.getCode());
                    materialDescription.setName(material.getName());
                    materialDescription.setSpecification(material.getSpecificationText());
                    materialDescription.setUnit(material.getBaseUnit());
                    materialDescription.setUnitText(dictService.getDictByTypeAndName("unit", material.getBaseUnit()).get().getLabel());
                    materialDescription.setCategoryId(material.getCategoryId());
                    materialDescription.setUnitPrice(material.getStandardPrice());
                    materialDescription.setCharacteristic("");
                }

                fillCharacteristicText(materialDescriptionList.stream().filter(materialDescriptionHandler -> Objects.nonNull(materialDescriptionHandler.getMaterialId())).collect(Collectors.toSet()));
            });
        }
    }

    public void consumeMaterialDescription(Collection<Long> materialIds, BiConsumer<Map<Long, Material>, DictService> consumer) {
        if (CollectionUtils.isEmpty(materialIds)) {
            return;
        }

        Map<Long, Material> idMaterialMap = materialDAO.selectByParamsWithoutCascade(Params.builder(1).pv("id", materialIds).build(), "id IN (:id)")
                .stream().collect(Collectors.toMap(Material::getId, material -> material));
        if (MapUtils.isNotEmpty(idMaterialMap)) {
            consumer.accept(idMaterialMap, dictService);
        }

    }

    private void handleClassification(List<Classification> classificationList) {
        if (CollectionUtils.isNotEmpty(classificationList)) {
            Map<String, com.rick.admin.module.core.entity.Classification> codeClassificationMap = classificationDAO.selectByCodesAsMap(classificationList.stream().map(Classification::getClassificationCode).collect(Collectors.toSet()));

            for (Classification classification : classificationList) {
                if (CollectionUtils.isEmpty(classification.getCharacteristicValueList())) {
                    Optional<com.rick.admin.module.core.entity.Classification> optional = classificationService.findByCode(classification.getClassificationCode());
                    classification.setCharacteristicValueList(optional.get().getCharacteristicList().stream()
                            .map(characteristic -> CharacteristicValue.builder().characteristicCode(characteristic.getCode()).characteristicId(characteristic.getId()).build()).collect(Collectors.toList()));
                }
            }

            Map<String, Long> codeCharacteristicMap = characteristicDAO.selectCodeIdMap(classificationList.stream().flatMap(classification -> classification.getCharacteristicValueList().stream()).map(CharacteristicValue::getCharacteristicCode).collect(Collectors.toSet()));

            for (Classification classification : classificationList) {
                classification.setClassification(codeClassificationMap.get(classification.getClassificationCode()));

                // value
                if (CollectionUtils.isNotEmpty(classification.getCharacteristicValueList())) {
                    for (CharacteristicValue characteristicValue : classification.getCharacteristicValueList()) {
                        characteristicValue.setClassificationCode(classification.getClassificationCode());
                        characteristicValue.setClassificationId(classification.getClassification().getId());
                        characteristicValue.setReferenceId(classification.getMaterialId());
                        characteristicValue.setCharacteristicId(codeCharacteristicMap.get(characteristicValue.getCharacteristicCode()));
                    }
                }
            }

            List<CharacteristicValue> characteristicValueList = classificationList.stream().flatMap(classification -> classification.getCharacteristicValueList().stream()).collect(Collectors.toList());
            // valid value
            List<com.rick.admin.module.core.entity.Classification> classificationListInternal
                    = classificationList.stream().map(Classification::getClassification).collect(Collectors.toList());

            // 物料特征值可以不填
//            characteristicHelper.validValueToClassification(classificationListInternal, characteristicValueList);
        }
    }

    private void handleMaterialProfile(Material material) {
        // 物料级别的profile
        Optional<MaterialProfile> materialProfileOptional = OptionalUtils.expectedAsOptional(
                materialProfileDAO.selectByParams(Params.builder(1).pv("materialCode", material.getCode()).build(), "material_code = :materialCode AND batch_code is NULL"));

        if (CollectionUtils.isEmpty(material.getClassificationList())) {
            if (materialProfileOptional.isPresent()) {
                materialProfileDAO.deleteById(materialProfileOptional.get().getId());
            }
            return;
        }

        MaterialProfile materialProfile;
        List<CharacteristicValue> characteristicValues = material.getClassificationList().stream().flatMap(classification -> classification.getCharacteristicValueList().stream()).collect(Collectors.toList());
        if (materialProfileOptional.isPresent()) {
            materialProfile = materialProfileOptional.get();
            materialProfile.setCharacteristicValueList(characteristicValues);
            materialProfile.setUpdateTime(null);
            materialProfile.setUpdateBy(null);
        } else {
            Long id = IdGenerator.getSequenceId();
            materialProfile = MaterialProfile.builder()
                    .id(id)
                    .category(MaterialProfile.CategoryEnum.MATERIAL)
                    .materialCode(material.getCode())
                    .materialId(material.getId())
                    .rootId(id)
                    .level(0)
                    .characteristicValueList(characteristicValues)
                    .build();
        }

        material.setMaterialProfile(materialProfile);
    }

    private void handleCharacteristic(Material material) {
        List<com.rick.admin.module.core.entity.Classification> classificationList
                = material.getClassificationList().stream().map(Classification::getClassification).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(classificationList)) {
            CharacteristicHelper.fillValueToClassification(classificationList, material.getMaterialProfile().getCharacteristicValueList());
        }
    }

    private void fillCharacteristicText(Collection<? extends MaterialDescriptionHandler> materialDescriptionHandlerList) {
        if (CollectionUtils.isNotEmpty(materialDescriptionHandlerList)) {
            Collection<String> materialIdBatchIdStringCollection = materialDescriptionHandlerList.stream().map(materialDescriptionHandler -> MaterialProfileSupport.materialIdBatchIdString(materialDescriptionHandler.getMaterialId(), materialDescriptionHandler.getBatchId())).collect(Collectors.toSet());
            Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);
            for (MaterialDescriptionHandler handler : materialDescriptionHandlerList) {
                handler.getMaterialDescription().setCharacteristic(StringUtils.defaultString(characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString(handler.getMaterialId(), handler.getBatchId())), ""));
            }
        }
    }
}