package com.rick.admin.module.core.service;

import com.google.common.collect.Lists;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.core.entity.Characteristic;
import com.rick.admin.module.core.model.CharacteristicDTO;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2022-06-25 14:01:00
 */
@RequiredArgsConstructor
@Service
public class CharacteristicManager {

    final EntityCodeDAO<Characteristic, Long> characteristicDAO;

    public CharacteristicDTO findById(Long id) {
        Characteristic characteristic = characteristicDAO.selectById(id).orElseThrow(() -> new ResourceNotFoundException("特征值"));
        return combine(characteristic);
    }

    public CharacteristicDTO findByCode(String code) {
        Characteristic characteristic = characteristicDAO.selectByCode(code).orElseThrow(() -> new ResourceNotFoundException("特征值"));
        return combine(characteristic);
    }

    public Map<Long, CharacteristicDTO> findByIdsAsMap(Collection<Long> ids) {
        List<CharacteristicDTO> characteristicDTOList = findByIds(ids);
        return characteristicDTOList.stream().collect(Collectors.toMap(CharacteristicDTO::getId, v -> v));
    }

    public List<CharacteristicDTO> findByIds(Collection<Long> ids) {
        List<Characteristic> characteristicList = characteristicDAO.selectByIds(ids);
        return getCharacteristicDTO(characteristicList);
    }

    public List<CharacteristicDTO> list(Long id, String code, String name) {
        List<Characteristic> characteristics = characteristicDAO.selectByParams(Params.builder(3).pv("id", id).pv("code", code).pv("name", name).build(), "id = :id AND code = :code and name like :name");
        return getCharacteristicDTO(characteristics);
    }

    private List<CharacteristicDTO> getCharacteristicDTO(List<Characteristic> characteristicList) {
        if (CollectionUtils.isEmpty(characteristicList)) {
            return Collections.emptyList();
        }

        List<CharacteristicDTO> characteristicDTOList = Lists.newArrayListWithExpectedSize(characteristicList.size());
        for (Characteristic characteristic : characteristicList) {
            characteristicDTOList.add(combine(characteristic));
        }

        return characteristicDTOList;
    }

    public static CharacteristicDTO combine(Characteristic characteristic) {
        CpnConfigurer configurer = characteristic.getCpnConfigurer();
        Map<String, Object> additionalInfo = configurer.getAdditionalInfo();
        return CharacteristicDTO.builder()
                .id(characteristic.getId())
                .code(characteristic.getCode())
                .description(characteristic.getDescription())
                .type(characteristic.getType())
                .options(configurer.getOptions())
                .required(CollectionUtils.isNotEmpty(configurer.getValidatorList()))
                .cpnType(configurer.getCpnType())
                .value(characteristic.getValue())
                .build();
    }
}
