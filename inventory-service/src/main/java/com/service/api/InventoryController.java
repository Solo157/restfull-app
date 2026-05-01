package com.service.api;

import com.service.database.Inventory;
import com.service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Контроллер биллинга. В этом контроллере все запросы требуют прохождения аутентификации, которая реализована через
 * сервис auth-сервис.
 */
@RestController
@RequestMapping("/api/inventory/v1")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Получить аккаунт.
     */
    @GetMapping("/inventory")
    public ResponseEntity<List<Inventory>> getInventory() {
        List<Inventory> inventory = inventoryService.getInventory();
        return ResponseEntity.status(HttpStatus.OK).body(inventory);

    }

    /**
     * Увеличить баланс аккаунта.
     */
    @PostMapping("/inventory/replenish")
    public ResponseEntity<String> replenishInventory(@RequestBody InventoryRequest request) {
        boolean replenished = inventoryService.replenishInventory(request.getProductName(), request.getCount());
        if (replenished) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Not replenish");
    }

    /**
     * Уменьшить баланс аккаунта.
     */
    @PostMapping("/inventory/reduce")
    public ResponseEntity<String> reduceInventory(@RequestBody InventoryRequest request) {
        boolean reduced = inventoryService.reduceInventory(request.getProductName(), request.getCount());
        if (reduced) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Not reduced");
    }

}
