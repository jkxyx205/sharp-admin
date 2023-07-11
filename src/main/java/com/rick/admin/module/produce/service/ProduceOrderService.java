package com.rick.admin.module.produce.service;

import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.db.plugin.dao.core.EntityDAO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author Rick.Xu
 * @date 2023/7/11 11:54
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class ProduceOrderService {

    EntityDAO<ProduceOrder, Long> produceOrderDAO;

    /**
     * 新增或修改
     * @param order
     */
    public void saveOrUpdate(ProduceOrder order) {
        if (order.getId() == null && StringUtils.isBlank(order.getCode())) {
            order.setCode(CodeHelper.generateCode("PDO"));
        }

        order.getItemList().forEach(item -> {
            item.setProduceOrderCode(order.getCode());
        });

        produceOrderDAO.insertOrUpdate(order);
    }

}