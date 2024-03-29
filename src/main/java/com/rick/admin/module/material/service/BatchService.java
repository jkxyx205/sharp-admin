package com.rick.admin.module.material.service;

import com.google.common.collect.Lists;
import com.rick.admin.module.core.entity.Characteristic;
import com.rick.admin.module.core.entity.Classification;
import com.rick.admin.module.material.dao.BatchDAO;
import com.rick.admin.module.material.dao.ClassificationDAO;
import com.rick.admin.module.material.entity.Batch;
import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.material.entity.MaterialProfile;
import com.rick.common.util.IdGenerator;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.support.EntityCodeIdFillService;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/4 18:03
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class BatchService {

    BatchDAO batchDAO;

    EntityCodeDAO<Classification, Long> classificationDAO;

    EntityCodeDAO<Characteristic, Long> characteristicDAO;

    EntityCodeIdFillService entityIdFillService;

    ClassificationDAO materialClassificationDAO;

    EntityDAO<MaterialProfile, Long> materialProfileDAO;

    MaterialProfileService materialProfileService;

    SharpService sharpService;

    /**
     * 分类引用的问题
     * @param itemList
     */
    public void handleClassificationAndFillCharacteristicValue(Collection<? extends BatchHandler> itemList) {
        Map<Long, List<com.rick.admin.module.material.entity.Classification>> materialIdClassificationMap = materialClassificationDAO.findMaterialClassificationByMaterialIds(itemList.stream().map(BatchHandler::getMaterialId).collect(Collectors.toSet()));

        itemList.forEach(item -> {
            List<com.rick.admin.module.material.entity.Classification> classificationList = materialIdClassificationMap.get(item.getMaterialId());
            if (CollectionUtils.isNotEmpty(classificationList)) {
                // 拷贝
                List<com.rick.admin.module.material.entity.Classification> newclassificationList = Lists.newArrayListWithExpectedSize(classificationList.size());
                for (com.rick.admin.module.material.entity.Classification classification : classificationList) {
                    newclassificationList.add(SerializationUtils.clone(classification));
                }
                classificationList = newclassificationList;
            } else {
                classificationList =  Collections.emptyList();
            }

            item.setClassificationList(classificationList);
        });

        fillCharacteristicValue(itemList);
    }

    public void fillCharacteristicValue(Collection<? extends BatchHandler> batchHandlerList) {
        if (CollectionUtils.isNotEmpty(batchHandlerList)) {
            Map<String, MaterialProfile> materialProfileMap = materialProfileService.getMaterialProfile(batchHandlerList.stream()
                            .filter(batchHandler -> Objects.nonNull(batchHandler.getMaterialId()))
                    .map(batchHandler -> MaterialProfileSupport.materialIdBatchIdString(batchHandler.getMaterialId(), batchHandler.getBatchId())).collect(Collectors.toSet()));

            // 获取特征值
            for (BatchHandler item : batchHandlerList) {
                if (CollectionUtils.isNotEmpty(item.getClassificationList())) {
                    MaterialProfile materialProfile = materialProfileMap.get(MaterialProfileSupport.materialIdBatchIdString(item.getMaterialId(), item.getBatchId()));
                    if (materialProfile != null) {
                        CharacteristicHelper.fillValueToClassification(item.getClassificationList().stream().map(classification -> classification.getClassification()).collect(Collectors.toList()),
                                materialProfile.getCharacteristicValueList());
                    }
                }
            }
        }

    }

    public void saveBatch(Collection<? extends BatchHandler> batchHandlerList) {
        // 处理批次物料
        if (CollectionUtils.isEmpty(batchHandlerList)) {
            return;
        }

        Set<String> materialIdBatchCodeStringCollection = batchHandlerList.stream().map(batchHandler -> batchHandler.getMaterialId() + BatchSupport.characteristicToCode(batchHandler.getClassificationList())).collect(Collectors.toSet());
        Map<String, Long> materialIdBatchCodeStringBatchIdMap = getMaterialIdBatchCodeStringBatchIdMap(materialIdBatchCodeStringCollection);

        for (BatchHandler item : batchHandlerList) {
            if (CollectionUtils.isNotEmpty(item.getClassificationList())) {
                String batchCode = BatchSupport.characteristicToCode(item.getClassificationList());
                Long batchId = materialIdBatchCodeStringBatchIdMap.get(item.getMaterialId() + batchCode);
                if (batchId == null) {
                    Batch batch = Batch.builder()
                            .code(batchCode)
                            .materialCode(item.getMaterialCode())
                            .materialId(item.getMaterialId())
                            .classificationList(item.getClassificationList())
                            .build();
                    saveOrUpdate(batch);

                    item.setBatchId(batch.getId());
                    item.setBatchCode(batchCode);
                } else {
                    item.setBatchId(batchId);
                    item.setBatchCode(batchCode);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(Batch batch) {
        boolean batchUpdate = true;
        if (batch.getId() == null) {
            Optional<Long> idOptional = batchDAO.selectIdByKeyCode(batch.getMaterialCode(), batch.getCode());
            if (idOptional.isPresent()) {
                batch.setId(idOptional.get());
            } else {
                batch.setId(IdGenerator.getSequenceId());
                batchUpdate = false;
            }
        }

        handleClassification(batch.getClassificationList(), batch.getId());

        if (CollectionUtils.isEmpty(batch.getClassificationList())) {
            batch.setClassificationList(Collections.emptyList());
        }


        List<CharacteristicValue> characteristicValueList = batch.getClassificationList().stream().flatMap(classification -> classification.getCharacteristicValueList().stream()).collect(Collectors.toList());
        // valid value
        List<Classification> classificationList
                = batch.getClassificationList().stream().map(com.rick.admin.module.material.entity.Classification::getClassification).collect(Collectors.toList());
        CharacteristicHelper.validValueToClassification(classificationList, characteristicValueList);

        batch.setMaterialId(entityIdFillService.fill(Material.class, batch.getMaterialId(), batch.getMaterialCode()));

        Long profileId;
        Optional<Long> profileIdOptional = materialProfileDAO.selectIdByParams(MaterialProfile.builder().batchCode(batch.getCode()).materialCode(batch.getMaterialCode()).build());
        boolean profileUpdate = profileIdOptional.isPresent();
        if (profileUpdate) {
            profileId = profileIdOptional.get();
        } else {
            profileId = IdGenerator.getSequenceId();
        }

        batch.setMaterialProfile(MaterialProfile.builder().id(profileId).build());

        if (batchUpdate) {
            batchDAO.update(batch);
        } else {
            batchDAO.insert(batch);
        }

        materialClassificationDAO.insertOrUpdate(batch.getClassificationList(), "material_id", batch.getMaterialId());

        handleMaterialProfile(batch, characteristicValueList, profileUpdate);
    }

    public Map<String, Long> getMaterialIdBatchCodeStringBatchIdMap(Collection<String> materialIdBatchCodeStringCollection) {
        return sharpService.queryForKeyValue("select concat(material_id, code), id from mm_batch where concat(material_id, code) IN (:materialIdBatchCodeStringCollection)"
                , Params.builder(1).pv("materialIdBatchCodeStringCollection", materialIdBatchCodeStringCollection).build());
    }

    private void handleClassification(List<com.rick.admin.module.material.entity.Classification> classificationList, Long batchId) {
        if (CollectionUtils.isNotEmpty(classificationList)) {
            Map<String, Classification> codeClassificationMap = classificationDAO.selectByCodesAsMap(classificationList.stream().map(com.rick.admin.module.material.entity.Classification::getClassificationCode).collect(Collectors.toSet()));
            Map<String, Long> codeCharacteristicMap = characteristicDAO.selectCodeIdMap(classificationList.stream().flatMap(classification -> classification.getCharacteristicValueList().stream()).map(CharacteristicValue::getCharacteristicCode).collect(Collectors.toSet()));

            for (com.rick.admin.module.material.entity.Classification classification : classificationList) {
                classification.setClassification(codeClassificationMap.get(classification.getClassificationCode()));

                // value
                if (CollectionUtils.isNotEmpty(classification.getCharacteristicValueList())) {
                    for (CharacteristicValue characteristicValue : classification.getCharacteristicValueList()) {
                        characteristicValue.setClassificationCode(classification.getClassificationCode());
                        characteristicValue.setClassificationId(classification.getClassification().getId());
                        characteristicValue.setReferenceId(batchId);
                        characteristicValue.setCharacteristicId(codeCharacteristicMap.get(characteristicValue.getCharacteristicCode()));
                    }
                }
            }
        }
    }

    private void handleMaterialProfile(Batch batch, List<CharacteristicValue> characteristicValueList, boolean update) {
        MaterialProfile materialProfile = MaterialProfile.builder()
                .id(batch.getMaterialProfile().getId())
                .category(MaterialProfile.CategoryEnum.BATCH)
                .materialCode(batch.getMaterialCode())
                .materialId(batch.getMaterialId())
                .batchId(batch.getId())
                .batchCode(batch.getCode())
                .rootId(batch.getMaterialProfile().getId())
                .level(0)
                .characteristicValueList(characteristicValueList)
                .build();

        if (update) {
            materialProfileDAO.update(materialProfile);
        } else {
            materialProfileDAO.insert(materialProfile);
        }
        batch.setMaterialProfile(materialProfile);
    }

}