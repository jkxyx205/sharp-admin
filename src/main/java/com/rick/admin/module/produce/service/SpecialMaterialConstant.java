package com.rick.admin.module.produce.service;

import java.util.HashSet;
import java.util.Set;

/**
 * 按照生产单领料的物料分类
 * @author Rick.Xu
 * @date 2024/7/21 19:08
 */
public final class SpecialMaterialConstant {

    private static final Set<Long> categoryIds = new HashSet<>();

    static {
        categoryIds.add(719895241229881344L); // 一体轮-轮毂
        categoryIds.add(719895198196322304L); // 辐条轮-轮毂
        categoryIds.add(725412890470801408L); // 线
        categoryIds.add(719897156139372544L); // 线包组件
        categoryIds.add(720625649131212800L); // 电机轴
        categoryIds.add(719895400634404864L); // 端盖
        categoryIds.add(725412754457911296L); // 霍尔线路板
        categoryIds.add(725413135871139840L); // 前轮
        categoryIds.add(719895859482873856L); // 前轴
    }

    public static boolean isSpecialSpecialMaterialCategory(long categoryId) {
        return categoryIds.contains(categoryId);
    }
}
