package com.service.service;

import com.service.adapter.OrderItemDTO;
import com.service.api.InventoryRequest;
import com.service.database.Inventory;
import com.service.database.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Главный сервис по работе со складом.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<Inventory> getInventory() {
        return inventoryRepository.findAll();
    }

    @Transactional
    public void replenishInventory(List<OrderItemDTO> itemDTOS) {
        for (OrderItemDTO itemDTO: itemDTOS) {
            boolean reduced = replenishInventory(itemDTO.getProductName(), itemDTO.getCount());
            if (!reduced) {
                throw new IllegalStateException("Not enough inventory");
            }
        }
    }

    /**
     * Пополнить склад товаром.
     */
    @Transactional
    public boolean replenishInventory(String productName, Integer count) {
        Optional<Inventory> existInventoryOpt = inventoryRepository.findByProductName(productName);
        if (existInventoryOpt.isEmpty()) {
            Inventory inventory = new Inventory();
            inventory.setProductName(productName);
            inventory.setCount(count);
            inventoryRepository.save(inventory);
            return true;
        }

        Inventory existInventory = existInventoryOpt.get();
        existInventory.setCount(existInventory.getCount() + count);

        inventoryRepository.save(existInventory);
        return true;
    }

    /**
     * Уменьшить склад товаром.
     */
    @Transactional
    public void reduceInventory(List<OrderItemDTO> itemDTOS) {
        for (OrderItemDTO itemDTO: itemDTOS) {
            boolean reduced = reduceInventory(itemDTO.getProductName(), itemDTO.getCount());
            if (!reduced) {
                throw new IllegalStateException("Not enough inventory");
            }
        }
    }

    /**
     * Снять деньги с аккаунта.
     */
    @Transactional
    public boolean reduceInventory(String productName, Integer count) {
        Optional<Inventory> existInventoryOpt = inventoryRepository.findByProductName(productName);
        if (existInventoryOpt.isEmpty()) {
            return false;
        }

        Inventory existInventory = existInventoryOpt.get();
        int commonCount = existInventory.getCount() - count;
        if (commonCount < 0) {
            return false;
        }

        existInventory.setCount(commonCount);
        inventoryRepository.save(existInventory);
        return true;
    }

}
