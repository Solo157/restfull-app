package com.service.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.commands.ReleaseInventoryCommand;
import com.service.adapter.commands.ReserveInventoryCommand;
import com.service.configuration.RabbitConfig;
import com.service.database.ProcessedCommand;
import com.service.database.ProcessedCommandsRepository;
import com.service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Обработчик команд из RabbitMQ для inventory-service.
 * Обрабатывает команды на резервирование и освобождение товаров на складе.
 */
@Component
@RequiredArgsConstructor
public class OrderServiceHandler {

    private final InventoryService inventoryService;
    private final ProcessedCommandsRepository processedCommandsRepository;
    private final OrderServiceProxy orderServiceProxy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.INVENTORY_RESERVE_COMMAND_QUEUE)
    public void handleInventoryReserveCommand(String messageBody) {
        System.out.println("Received message: " + messageBody);

        ReserveInventoryCommand command = deserializeCommand(messageBody, ReserveInventoryCommand.class);
        if (command == null) {
            return;
        }

        final UUID sagaId = command.getSagaId();
        final Long orderId = command.getOrderId();
        final List<OrderItemDTO> items = command.getItems();
        final String idempotencyKey = command.getKeyIdempotence();

        if (!saveProcessedCommand(sagaId, orderId, idempotencyKey)) {
            orderServiceProxy.sendInventoryReservedEvent(sagaId, orderId, false, "reserved command command already done");
            return;
        }

        try {
            inventoryService.reduceInventory(items);
        } catch (Exception e) {
            e.printStackTrace();
            orderServiceProxy.sendInventoryReservedEvent(sagaId, orderId, false, "inventory for order is not reserved");
            return;
        }

        orderServiceProxy.sendInventoryReservedEvent(sagaId, orderId, true, "inventory for order reserved");
    }

    @RabbitListener(queues = RabbitConfig.INVENTORY_RELEASE_COMMAND_QUEUE)
    public void handleInventoryReleaseCommand(String messageBody) {
        System.out.println("Received message: " + messageBody);

        ReleaseInventoryCommand command = deserializeCommand(messageBody, ReleaseInventoryCommand.class);
        if (command == null) {
            return;
        }

        final UUID sagaId = command.getSagaId();
        final Long orderId = command.getOrderId();
        final List<OrderItemDTO> items = command.getItems();
        final String idempotencyKey = command.getKeyIdempotence();

        if (!saveProcessedCommand(sagaId, orderId, idempotencyKey)) {
            orderServiceProxy.sendInventoryReleasedEvent(sagaId, orderId, false, "released command command already done");
            return;
        }

        try {
            inventoryService.replenishInventory(items);
        } catch (Exception e) {
            e.printStackTrace();
            orderServiceProxy.sendInventoryReleasedEvent(sagaId, orderId, false, "inventory for order is not released");
            return;
        }

        orderServiceProxy.sendInventoryReleasedEvent(sagaId, orderId, true, "inventory for order released");
    }

    private <T> T deserializeCommand(String messageBody, Class<T> commandType) {
        try {
            return objectMapper.readValue(messageBody, commandType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public boolean saveProcessedCommand(UUID sagaId, Long orderId, String idempotencyKey) {
        try {
            ProcessedCommand processedCommand = new ProcessedCommand();
            processedCommand.setSagaId(sagaId);
            processedCommand.setOrderId(orderId);
            processedCommand.setIdempotencyKey(idempotencyKey);
            processedCommandsRepository.save(processedCommand);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
