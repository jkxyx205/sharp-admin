package com.rick.admin.module.material.dao;

import com.rick.admin.module.material.entity.Material;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.plugin.dao.core.ColumnFillType;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/6/2 10:36
 */
@Repository
public class MaterialDAO extends EntityCodeDAOImpl<Material, Long> {

    /**
     * 第一个参数是价格；第二个参数是物料id
     * @param paramsList
     * @return
     */
    public int[] updatePrice(List<Object[]> paramsList) {
        if (CollectionUtils.isEmpty(paramsList)) {
            return new int[0];
        }

        return SQLUtils.update(getTableName(), "standard_price", paramsList, "id = ?");
    }

    @Override
    protected Object[] handleAutoFill(Object t, Object[] params, List<String> columnNameList, ColumnFillType fillType) {
        Object[] objects = super.handleAutoFill(t, params, columnNameList, fillType);
        Material material = (Material)t;
        material.getMaterialProfile().setMaterialId(material.getId());
        return objects;
    }
}