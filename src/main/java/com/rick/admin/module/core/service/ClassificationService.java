package com.rick.admin.module.core.service;

import com.google.common.collect.Lists;
import com.rick.admin.module.core.entity.Characteristic;
import com.rick.admin.module.core.entity.Classification;
import com.rick.admin.module.core.model.CharacteristicDTO;
import com.rick.admin.module.core.model.ClassificationDTO;
import com.rick.admin.module.core.model.ClassificationForm;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @date  2022-06-29 13:18:00
 */
@Service
@RequiredArgsConstructor
@Validated
public class ClassificationService {

    final EntityCodeDAO<Characteristic, Long> characteristicDAO;

    final EntityCodeDAO<Classification, Long> classificationDAO;

    private final CharacteristicManager characteristicManager;

    @Transactional(rollbackFor = Exception.class)
    public int saveOrUpdate(ClassificationDTO classificationDTO) {
        Classification classification = Classification.builder().id(classificationDTO.getId())
                .description(classificationDTO.getDescription()).code(classificationDTO.getCode())
                .build();

        characteristicDAO.assertCodesExistsAndUnDuplicate(classificationDTO.getCharacteristicCodes());

        if (CollectionUtils.isNotEmpty(classificationDTO.getCharacteristicCodes())) {
            List<Long> characteristicIds = characteristicDAO.selectIdsByCodes(classificationDTO.getCharacteristicCodes());
            List<Characteristic> list = Lists.newArrayListWithExpectedSize(classificationDTO.getCharacteristicCodes().size());
            for (Long characteristicId : characteristicIds) {
                list.add(Characteristic.builder().id(characteristicId).build());
            }

            classification.setCharacteristicList(list);
        }

        int count = classificationDAO.insertOrUpdate(classification);
        classificationDTO.setId(classification.getId());
        return count;
    }

    public Optional<Classification> findById(Long id) {
        return classificationDAO.selectById(id);
    }

    public Optional<Classification> findByCode(String code) {
        return classificationDAO.selectByCode(code);
    }

    public ClassificationForm findClassificationFormByCode(String code) {
        Classification classification = classificationDAO.selectByCode(code).get();
        List<Long> characteristicIds = classification.getCharacteristicList().stream().map(Characteristic::getId).collect(Collectors.toList());
        List<CharacteristicDTO> characteristicDTOList;
        if (CollectionUtils.isNotEmpty(characteristicIds)) {
            characteristicDTOList = characteristicManager.findByIds(characteristicIds);
        } else {
            characteristicDTOList = Collections.emptyList();
        }
        return new ClassificationForm(classification, characteristicDTOList);
    }

    public int deleteById(Long id) {
        return classificationDAO.deleteById(id);
    }

}