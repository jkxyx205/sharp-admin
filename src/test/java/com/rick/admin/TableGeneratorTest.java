package com.rick.admin;

import com.rick.admin.module.purchase.entity.LatestPrice;
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

//        tableGenerator.createTable(BomTemplate.class);
//        tableGenerator.createTable(BomTemplate.Component.class);
//        tableGenerator.createTable(BomTemplate.ComponentDetail.class);
//        tableGenerator.createTable(MaterialSource.class);

//        tableGenerator.createTable(Characteristic.class);
//        tableGenerator.createTable(Classification.class);

//        tableGenerator.createTable(CharacteristicValue.class);
//        tableGenerator.createTable(Classification.class);
//        tableGenerator.createTable(MaterialProfile.class);
//        tableGenerator.createTable(Batch.class);

//        tableGenerator.createTable(ProduceOrder.class);
//        tableGenerator.createTable(ProduceOrder.Item.class);
//        tableGenerator.createTable(ProduceOrder.Item.Detail.class);
//        tableGenerator.createTable(ProduceOrder.Item.Schedule.class);
//
//        tableGenerator.createTable(ContactInfo.class);
//        tableGenerator.createTable(PurchaseRequisition.class);
//        tableGenerator.createTable(PurchaseRequisition.Item.class);

//        tableGenerator.createTable(CodeSequence.class);
        tableGenerator.createTable(LatestPrice.class);

    }
}
