package com.rick.admin;

import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.db.plugin.dao.core.TableGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Rick.Xu
 * @date 2023/5/27 18:41
 */
@SpringBootTest
public class TableGeneratorTest {

    @Autowired
    private TableGenerator tableGenerator;

    @Test
    public void generateTable() {
//        tableGenerator.createTable(User.class);
//        tableGenerator.createTable(Role.class);
//        tableGenerator.createTable(Permission.class);
//        tableGenerator.createTable(Partner.class);
//        tableGenerator.createTable(Material.class);
//        tableGenerator.createTable(InventoryDocument.class);
//        tableGenerator.createTable(InventoryDocument.Item.class);
//        tableGenerator.createTable(Stock.class);
//        tableGenerator.createTable(PurchaseOrder.class);
//        tableGenerator.createTable(PurchaseOrder.Item.class);
//        tableGenerator.createTable(Bom.class);
//        tableGenerator.createTable(Bom.Item.class);
//        tableGenerator.createTable(ProduceOrder.class);
//        tableGenerator.createTable(ProduceOrder.Item.class);

        tableGenerator.createTable(BomTemplate.class);
        tableGenerator.createTable(BomTemplate.Component.class);
        tableGenerator.createTable(BomTemplate.ComponentDetail.class);

    }
}
