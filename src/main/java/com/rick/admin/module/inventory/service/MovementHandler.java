package com.rick.admin.module.inventory.service;

import com.rick.admin.module.inventory.entity.InventoryDocument;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:38
 */
public interface MovementHandler {

    /**
     * 移动场景
     * @return 移动场景
     */
    InventoryDocument.TypeEnum type();

    /**
     * 参考类型
     * @return 参考类型
     */
    InventoryDocument.ReferenceTypeEnum reference();

    /**
     * 处理
     * @param inventoryDocument 移动凭证
     */
    void handle(InventoryDocument inventoryDocument);

}
