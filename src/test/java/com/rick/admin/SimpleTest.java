package com.rick.admin;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/11 14:12
 */
public class SimpleTest {

    @Test
    public void test() {
        String sql = " SELECt mm_material.id, mm_material.code, mm_material.name, characteristic, case when attachment is null or length(attachment) <= 2 then '无' else '有' end  attachment, material_type, mm_material.category_id, base_unit, standard_price, sys_user.name create_name,DATE_FORMAT(mm_material.create_time, '%Y-%m-%d %H:%i:%s') create_time from mm_material left join sys_user on sys_user.id = mm_material.create_by WHERE mm_material.code = :code AND mm_material.name like :name AND material_type = :materialType AND category_id = :categoryId AND mm_material.is_deleted = 0"
                .replaceFirst("(?i)(select)\\s+", "SELECT ")
                .replaceFirst("(?i)\\s+(from)\\s+", " FROM ")
                .replaceFirst("(?i)\\s+(WHERE)\\s+", " WHERE ");

        String queryColumnNames = StringUtils.substringBetween(sql, "SELECT ", " FROM");

        List<String> collect = Arrays.stream(queryColumnNames.split("\\s*,\\s*")).collect(Collectors.toList());

        System.out.println(collect);

        System.out.println();
    }
}
