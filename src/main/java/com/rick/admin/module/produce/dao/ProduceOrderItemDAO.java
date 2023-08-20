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
public class ProduceOrderItemDAO extends EntityDAOImpl<ProduceOrder.Item, Long> {
}