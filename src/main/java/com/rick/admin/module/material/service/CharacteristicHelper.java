package com.rick.admin.module.material.service;

import com.google.common.collect.Lists;
import com.rick.admin.module.core.entity.Characteristic;
import com.rick.admin.module.core.entity.Classification;
import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.common.util.JsonUtils;
import com.rick.formflow.form.cpn.core.CpnInstanceProcessor;
import com.rick.formflow.form.cpn.core.CpnManager;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.formflow.form.valid.core.Validator;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.MapBindingResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2023-02-23 17:56:00
 */
@UtilityClass
public class CharacteristicHelper {

    public void handlerSameReference(List<? extends BatchHandler> batchHandlerList) {
        // 处理分类的重复引用问题
        for (BatchHandler batchHandler : batchHandlerList) {
            List<com.rick.admin.module.material.entity.Classification> classificationList = batchHandler.getClassificationList();
            if (CollectionUtils.isEmpty(classificationList)) {
                continue;
            }

            List<com.rick.admin.module.material.entity.Classification> newClassification = Lists.newArrayListWithExpectedSize(classificationList.size());
            for (com.rick.admin.module.material.entity.Classification classification : classificationList) {
                newClassification.add(SerializationUtils.clone(classification));
            }
            batchHandler.setClassificationList(newClassification);
        }
    }

    public void validValueToClassification(List<Classification> classificationList, List<CharacteristicValue> characteristicValueList) {
        if (CollectionUtils.isEmpty(classificationList)) {
            return;
        }

        Map<String, String> characteristicValueMap;
        if (CollectionUtils.isEmpty(characteristicValueList)) {
            characteristicValueMap = Collections.emptyMap();
        } else {
            characteristicValueMap = characteristicValueList.stream().collect(Collectors.toMap(cv -> cv.getClassificationCode() + ":"+ cv.getCharacteristicCode(), cv -> StringUtils.defaultIfBlank(cv.getValue(), "")));
        }
        for (Classification classification : classificationList) {
            for (Characteristic characteristic : classification.getCharacteristicList()) {
                MapBindingResult result = new MapBindingResult(new HashMap<>(), characteristic.getDescription());
                String value = characteristicValueMap.get(classification.getCode() + ":" +characteristic.getCode());

                List<String> valueObject;

                if (characteristic.getCpnConfigurer().getCpnType() == CpnTypeEnum.CHECKBOX && StringUtils.isNotBlank(value)) {
                    valueObject = JsonUtils.toList(value, String.class);
                } else {
                    valueObject = Lists.newArrayList(value);
                }

                for (String val : valueObject) {
                    // 验证输入值的合法性
                    if (CollectionUtils.isNotEmpty(characteristic.getCpnConfigurer().getOptions())) {
                        if (characteristic.getType() == Characteristic.CharacteristicTypeEnum.DATE) {
                            Set<Validator> validators = CpnManager.getCpnByType(CpnTypeEnum.DATE).cpnValidators();
                            for (Validator cpnValidator : validators) {
                                cpnValidator.valid(val);
                            }
                        } else if (characteristic.getType() == Characteristic.CharacteristicTypeEnum.TIME) {
                            Set<Validator> validators = CpnManager.getCpnByType(CpnTypeEnum.TIME).cpnValidators();
                            for (Validator cpnValidator : validators) {
                                cpnValidator.valid(val);
                            }
                        } else if (characteristic.getType() == Characteristic.CharacteristicTypeEnum.CURRENCY ||
                                characteristic.getType() == Characteristic.CharacteristicTypeEnum.NUMBER) {
                            Set<Validator> validators = CpnManager.getCpnByType(CpnTypeEnum.NUMBER_TEXT).cpnValidators();
                            for (Validator cpnValidator : validators) {
                                cpnValidator.valid(val);
                            }
                        }
                    }

                    CpnInstanceProcessor cpnInstanceProcessor = new CpnInstanceProcessor(new FormBO.Property(characteristic.getId(), characteristic.getDescription(), characteristic.getCpnConfigurer(), val)
                            , val, result);

                    cpnInstanceProcessor.valid();

                    if (result.hasErrors()) {
                        throw new IllegalArgumentException(result.getAllErrors().toString());
                    }
                    // endregion
                }
            }
        }

    }

    /**
     * 注意 确保不能有相同的引用， handlerSameReference 处理过的确保没有重复引用
     * @param classificationList
     * @param characteristicValueList
     */
    public void fillValueToClassification(List<Classification> classificationList, List<CharacteristicValue> characteristicValueList) {
        if (CollectionUtils.isNotEmpty(characteristicValueList)) {
            MultiKeyMap map = new MultiKeyMap();

            for (CharacteristicValue characteristicValue : characteristicValueList) {
                map.put(characteristicValue.getClassificationId(), characteristicValue.getCharacteristicId(), characteristicValue.getValue());
            }

            for (Classification classification : classificationList) {
                if (CollectionUtils.isNotEmpty(classification.getCharacteristicList())) {
                    Iterator<Characteristic> iterator = classification.getCharacteristicList().iterator();
                    while (iterator.hasNext()) {
                        Characteristic characteristic = iterator.next();
                        characteristic.getCpnConfigurer().setDefaultValue((String) map.get(classification.getId(), characteristic.getId()));
                    }
                }
            }
        }
    }

    public boolean isSameCharacteristicValue(List<CharacteristicValue> firstCharacteristicValueList, List<CharacteristicValue> secondCharacteristicValueList) {
        if (firstCharacteristicValueList.size() != secondCharacteristicValueList.size()) {
            return false;
        }

        Map<String, String> firstValueMap = firstCharacteristicValueList.stream().collect(Collectors.toMap(cv -> cv.getClassificationCode() + ":" + cv.getCharacteristicCode(), cv -> StringUtils.defaultIfBlank(cv.getValue(), "")));
        for (CharacteristicValue characteristicValue : secondCharacteristicValueList) {
            if (!Objects.equals(StringUtils.defaultString(characteristicValue.getValue(), ""), firstValueMap.get(characteristicValue.getClassificationCode() + ":" + characteristicValue.getCharacteristicCode()))) {
                return false;
            }
        }

        return true;
    }
}
