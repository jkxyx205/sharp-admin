package com.rick.admin.module.produce.dao;

import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2023/8/20 18:18
 */
@Repository
@RequiredArgsConstructor
public class ProduceOrderItemDetailDAO extends EntityDAOImpl<ProduceOrder.Item.Detail, Long> {

//    private final ClassificationDAO materialClassificationDAO;

//    @Override
//    protected <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, SqlHandler sqlHandler, Class<E> clazz) {
//        List<E> list = super.selectByParams(params, columnNames, conditionSQL, sqlHandler, clazz);
//        for (E e : list) {
//            ProduceOrder.Item.Detail item = (ProduceOrder.Item.Detail) e;
//            // 手动加载防止两个相同的物料， 特征分组是相同的引用
//            item.setClassificationList(materialClassificationDAO.selectByParams(Classification.builder().materialId(item.getMaterialId()).build()));
//        }
//        return list;
//    }
}