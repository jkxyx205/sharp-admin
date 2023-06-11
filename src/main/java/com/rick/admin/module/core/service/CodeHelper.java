package com.rick.admin.module.core.service;

import com.rick.common.util.Time2StringUtils;
import lombok.experimental.UtilityClass;

import java.time.Instant;

/**
 * @author Rick.Xu
 * @date 2023/6/11 12:13
 */
@UtilityClass
public class CodeHelper {

    public String generateCode(String prefix) {
        return prefix + Time2StringUtils.format(Instant.now()).replaceAll("\\s+|-|:", "");
    }
}
