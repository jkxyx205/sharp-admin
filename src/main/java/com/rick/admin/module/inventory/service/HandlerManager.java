package com.rick.admin.module.inventory.service;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:46
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HandlerManager {

    private final List<MovementHandler> movementHandlerList;

    @Transactional(rollbackFor = Exception.class)
    public void handle(InventoryDocument inventoryDocument) {
        MovementHandler determinMovementHandler = null;
        for (MovementHandler movementHandler : movementHandlerList) {
            if (movementHandler.type() == inventoryDocument.getType() && movementHandler.reference() == inventoryDocument.getReferenceType()) {
                determinMovementHandler = movementHandler;
                break;
            }
        }

        if (Objects.isNull(determinMovementHandler)) {
            log.error("没有找到合适的 MovementHandler");
        }

        determinMovementHandler.handle(inventoryDocument);
    }

}
