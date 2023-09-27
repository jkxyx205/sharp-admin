package com.rick.admin.module.core.service;

import com.rick.admin.module.core.entity.ContactInfo;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/9/26 10:29
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class ContactInfoService {

    EntityDAO<ContactInfo, Long> contactInfoDAO;

    public Map<Long, ContactInfo> getInstanceIdEntityMap(Collection<Long> instanceIds) {
        Assert.notEmpty(instanceIds, "instantIds cannot be empty");
        return contactInfoDAO.selectByParams(Params.builder(1).pv("instanceIds", instanceIds).build(),
                "instance_id IN (:instanceIds)")
                .stream().collect(Collectors.toMap(ContactInfo::getInstanceId, Function.identity()));
    }

}