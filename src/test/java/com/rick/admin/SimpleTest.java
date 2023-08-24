package com.rick.admin;

import com.rick.admin.module.material.entity.Material;
import com.rick.common.util.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
        String sql = " SELECt mm_material.id, mm_material.code, mm_material.name, specification, case when attachment is null or length(attachment) <= 2 then '无' else '有' end  attachment, material_type, mm_material.category_id, base_unit, standard_price, sys_user.name create_name,DATE_FORMAT(mm_material.create_time, '%Y-%m-%d %H:%i:%s') create_time from mm_material left join sys_user on sys_user.id = mm_material.create_by WHERE mm_material.code = :code AND mm_material.name like :name AND material_type = :materialType AND category_id = :categoryId AND mm_material.is_deleted = 0"
                .replaceFirst("(?i)(select)\\s+", "SELECT ")
                .replaceFirst("(?i)\\s+(from)\\s+", " FROM ")
                .replaceFirst("(?i)\\s+(WHERE)\\s+", " WHERE ");

        String queryColumnNames = StringUtils.substringBetween(sql, "SELECT ", " FROM");

        List<String> collect = Arrays.stream(queryColumnNames.split("\\s*,\\s*")).collect(Collectors.toList());

        System.out.println(collect);

        System.out.println();
    }

    @Test
    public void test2() throws NoSuchFieldException {
        Field attachmentList = ClassUtils.getField(Material.class, "attachmentList");
        Class<?> genericClass = ClassUtils.getFieldGenericClass(attachmentList);
        System.out.println(genericClass);
    }

    @Test
    public void test3() throws NoSuchFieldException {
        Field classificationList = ClassUtils.getField(Material.class, "classificationList");
        Class<?> genericClass = ClassUtils.getFieldGenericClass(classificationList);
        System.out.println(genericClass);
    }

    @Test
    public void test4() {
        List<List<String>> data =new ArrayList<>();
        data.add(Arrays.asList("1", "2", "3"));
        data.add(Arrays.asList("4", "5", "6"));
        data.add(Arrays.asList("7", "8", "9"));
        data.add(Arrays.asList("a", "b", "c"));

        dd(data, 0, "");
    }

    private void dd(List<List<String>> data, int index, String value) {
        if (index >= data.size()) {
            return;
        }

        for (String s : data.get(index)) {
            if (index == data.size() - 1) {
                System.out.print((value + " ") + s);
            }
            dd(data, index + 1, (StringUtils.isBlank(value) ? "" : value + " ") + s);
            System.out.println();
        }
    }
}
