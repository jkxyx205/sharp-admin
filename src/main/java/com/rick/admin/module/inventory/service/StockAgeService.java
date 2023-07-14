package com.rick.admin.module.inventory.service;

import com.google.common.collect.Maps;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/7/14 16:30
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StockAgeService {

    SharpService sharpService;

    public Map getAge(long materialId, long stockQuantity) {
        Map<String, BigDecimal> map = Maps.newHashMapWithExpectedSize(4);

        map.put("0", BigDecimal.ZERO);
        map.put("1", BigDecimal.ZERO);
        map.put("2", BigDecimal.ZERO);
        map.put("3", BigDecimal.ZERO);

        if (stockQuantity > 0) {
            String  querySql = "select inv_document_item.quantity,inv_document_item.create_time from inv_document_item \n" +
                    "\n" +
                    "left join (select * from (\n" +
                    "select s1.id, s1.material_id, s1.quantity, s1.create_time,\n" +
                    "(select sum(s2.quantity) from (select id, quantity,create_time from inv_document_item where movement_type = 'INBOUND' AND type = 'INBOUND'\n" +
                    "and material_id = :materialId order by id desc) s2 where id>=s1.id) as cum_sales\n" +
                    "from (select id, material_id, quantity,create_time from inv_document_item where movement_type = 'INBOUND' AND type = 'INBOUND'\n" +
                    "and material_id = :materialId order by id desc) s1\n" +
                    ") f where f.cum_sales >= :stockQuantity limit 0, 1) cc on cc.material_id = inv_document_item.material_id\n" +
                    "\n" +
                    "where inv_document_item.movement_type = 'INBOUND' AND inv_document_item.type = 'INBOUND'\n" +
                    "and inv_document_item.material_id = :materialId\n" +
                    "and inv_document_item.id >= cc.id\n" +
                    " order by inv_document_item.id desc";

            Map<String, Object> params = Params.builder(1)
                    .pv("stockQuantity", stockQuantity)
                    .pv("materialId", materialId)
                    .build();

            List<Map<String, Object>> list = sharpService.query(querySql, params);

            if (CollectionUtils.isEmpty(list)) {
                querySql = "select quantity, create_time from inv_document_item where movement_type = 'INBOUND' AND type = 'INBOUND'\n" +
                        "and material_id = :materialId\n" +
                        " order by id desc";

                list = sharpService.query(querySql, params);

                BigDecimal total = list.stream().map(m -> (BigDecimal) m.get("quantity")).reduce(BigDecimal.ZERO, (n1, n2) -> n1.add(n2));

                Map<String, Object> lastRow = Maps.newHashMapWithExpectedSize(2);
                lastRow.put("quantity", BigDecimal.valueOf(stockQuantity).subtract(total));
                lastRow.put("create_time", LocalDateTime.MIN);
                list.add(lastRow);
            } else {
                // 根据 stockQuantity 调整最后一个的数量
                BigDecimal total = list.stream().map(m -> (BigDecimal) m.get("quantity")).reduce(BigDecimal.ZERO, (n1, n2) -> n1.add(n2));
                BigDecimal lastQuantity = (BigDecimal) list.get(list.size() - 1).get("quantity");
                list.get(list.size() - 1).put("quantity", BigDecimal.valueOf(stockQuantity).subtract(total.subtract(lastQuantity)));
            }

            // 计算周期
            for (Map<String, Object> row : list) {
                long diffDays = ChronoUnit.DAYS.between((Temporal) row.get("create_time"), LocalDateTime.now());
                String ageArea = "3";
                if (diffDays <= 7) {
                    ageArea = "0";
                } else if (diffDays <= 30) {
                    ageArea = "1";
                } else if (diffDays <= 90) {
                    ageArea = "2";
                }

                row.put("ageArea", ageArea);
                map.put(ageArea, map.get(ageArea).add((BigDecimal) row.get("quantity")));
            }
        }

        return map;
    }
}
