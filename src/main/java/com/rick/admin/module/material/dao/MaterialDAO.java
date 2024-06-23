package com.rick.admin.module.material.dao;

import com.rick.admin.module.material.entity.Material;
import com.rick.db.plugin.dao.core.ColumnFillType;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import com.rick.db.service.support.Params;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/6/2 10:36
 */
@Repository
public class MaterialDAO extends EntityCodeDAOImpl<Material, Long> {

//    /**
//     * 第一个参数是价格；第二个参数是物料id
//     * @param paramsList
//     * @return
//     */
////    public int[] updatePrice(List<Object[]> paramsList) {
//        if (CollectionUtils.isEmpty(paramsList)) {
//            return new int[0];
//        }
//
//        return SQLUtils.update(getTableName(), "standard_price", paramsList, "id = ?");
//    }

    public List<String> getSpecificationByCategory(Long categoryId) {
        return selectByParams(Params.builder(1).pv("categoryId", categoryId).build(),
                "specification", "category_id = :categoryId", String.class);
    }

    public List<Map> getNameAndSpecificationByCategory(Long categoryId) {
        return selectByParams(Params.builder(1).pv("categoryId", categoryId).build(),
                "name, specification", "category_id = :categoryId", Map.class);
    }

    @Override
    protected Object[] handleAutoFill(Object t, Object[] params, List<String> columnNameList, ColumnFillType fillType) {
        Object[] objects = super.handleAutoFill(t, params, columnNameList, fillType);

        if (t != null) {
            // 提前设置外键material_id
            Material material = (Material)t;
            if (Objects.nonNull(material.getMaterialProfile())) {
                material.getMaterialProfile().setMaterialId(material.getId());
            }
        }

        return objects;
    }
}