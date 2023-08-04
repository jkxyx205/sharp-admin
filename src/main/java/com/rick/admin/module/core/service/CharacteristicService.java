package com.rick.admin.module.core.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rick.admin.module.core.entity.Characteristic;
import com.rick.admin.module.core.model.CharacteristicDTO;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.formflow.form.cpn.Date;
import com.rick.formflow.form.cpn.Text;
import com.rick.formflow.form.cpn.Time;
import com.rick.formflow.form.cpn.core.*;
import com.rick.formflow.form.valid.DecimalRegex;
import com.rick.formflow.form.valid.Required;
import com.rick.formflow.form.valid.core.Validator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author Rick.Xu
 * @date 2023/8/4 10:10
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class CharacteristicService {

    EntityCodeDAO<Characteristic, Long> characteristicDAO;

    JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(CharacteristicDTO characteristicDTO) {
        Optional<Long> idOptional = characteristicDAO.selectIdByCode(characteristicDTO.getCode());
        if (Objects.isNull(characteristicDTO.getId())) {
            idOptional.ifPresent(id -> characteristicDTO.setId(id));
        }

        checkIfAvailable(characteristicDTO);
        CpnConfigurer configurer = createConfigurer(characteristicDTO);
        CpnManager.getCpnByType(configurer.getCpnType()).check(configurer);

        Characteristic characteristic = Characteristic.builder()
                .id(characteristicDTO.getId())
                .code(characteristicDTO.getCode())
                .description(characteristicDTO.getDescription())
                .cpnConfigurer(configurer)
                .type(characteristicDTO.getType())
                .build();

        cleanConfig(characteristic.getId());
        characteristicDAO.insertOrUpdate(characteristic);
        characteristicDTO.setId(characteristic.getId());

    }

    private CpnConfigurer createConfigurer(CharacteristicDTO characteristicDTO) {
        List<Validator> requiredValidator = Boolean.TRUE.equals(characteristicDTO.getRequired()) ? Lists.newArrayListWithExpectedSize(1) : null;
        if (Objects.nonNull(requiredValidator)) {
            requiredValidator.add(new Required(true));
        }

        CpnConfigurer configurer = CpnConfigurer.builder()
                .name(characteristicDTO.getCode())
                .label(characteristicDTO.getDescription())
                .validatorList(requiredValidator)
                .options(characteristicDTO.getOptions())
                .cpnType(characteristicDTO.getCpnType())
                .additionalInfo(characteristicDTO.getAdditionalInfo())
                .disabled(false)
                .build();

        return configurer;
    }

    private void checkIfAvailable(CharacteristicDTO characteristicDTO) {
        Cpn cpn = new Text();
        if (characteristicDTO.getType() == Characteristic.CharacteristicTypeEnum.NUMBER || characteristicDTO.getType() == Characteristic.CharacteristicTypeEnum.CURRENCY) {
            cpn = new AbstractCpn<String>() {
                @Override
                public CpnTypeEnum getCpnType() {
                    return CpnTypeEnum.NUMBER_TEXT;
                }

                @Override
                public Set<Validator> cpnValidators() {
                    return Sets.newHashSet(new Validator[]{new DecimalRegex()});
                }
            };
        } else if (characteristicDTO.getType() == Characteristic.CharacteristicTypeEnum.DATE) {
            cpn = new Date();
        } else if (characteristicDTO.getType() == Characteristic.CharacteristicTypeEnum.TIME) {
            cpn = new Time();
        }

        if (CollectionUtils.isNotEmpty(characteristicDTO.getOptions())) {
            for (CpnConfigurer.CpnOption option : characteristicDTO.getOptions()) {
                Set<Validator> validators = cpn.cpnValidators();
                for (Validator cpnValidator : validators) {
                    if (StringUtils.isBlank(option.getLabel())) {
                        throw new BizException(ResultUtils.fail(50011, "选项不能为空"));
                    }
                    cpnValidator.valid(option.getLabel());
                }
            }
        }
    }

    private void cleanConfig(Long id) {
        if (id == null) {
            return;
        }
        // language=SQL
        String cleanSql = "DELETE " +
                "FROM sys_form_configurer\n" +
                "WHERE EXISTS(SELECT 1\n" +
                "             FROM core_characteristic\n" +
                "             WHERE sys_form_configurer.id = core_characteristic.config_id\n" +
                "               AND core_characteristic.id = ?)";

        jdbcTemplate.update(cleanSql, id);
    }

}