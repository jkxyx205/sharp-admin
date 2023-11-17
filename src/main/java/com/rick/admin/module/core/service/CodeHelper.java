package com.rick.admin.module.core.service;

import com.rick.common.util.Time2StringUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Instant;
import java.util.function.Function;

/**
 * @author Rick.Xu
 * @date 2023/6/11 12:13
 */
@UtilityClass
public class CodeHelper {

    public String generateCode(String prefix) {
        return prefix + getTimestamp(time -> time.replaceAll("\\s+|-|:", ""));
    }

    public String generateCodeWithDay(String prefix) {
        return prefix + getTimestamp(time -> time.replaceAll("\\s+|-|:", "").substring(0, 8));
    }

    private String getTimestamp(Function<String, String> formatTimestamp) {
        String timestamp = formatTimestamp.apply(Time2StringUtils.format(Instant.now()));
        timestamp = String.valueOf(Long.parseLong(timestamp) + Long.parseLong(RandomStringUtils.randomNumeric(5)));
        return timestamp;
    }

}
