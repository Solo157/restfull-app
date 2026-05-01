package com.service.adapter;

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

import java.util.*;

/**
 * Обработчик сообщений от RabbitMQ.
 */
@Component
@RequiredArgsConstructor
public class OrderServiceHandler {

    private final InventoryService inventoryService;
    private final ProcessedCommandsRepository processedCommandsRepository;
    private final OrderServiceProxy orderServiceProxy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Обработка приходящего сообщения по созданному заказу и поэтому требуется его обработать - снять деньги со счета.
     */
    @RabbitListener(queues = RabbitConfig.INVENTORY_RESERVE_COMMAND_QUEUE)
    public void handleReservePaymentCommand(String messageBody) {
        ReserveInventoryCommand event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, ReserveInventoryCommand.class);
            System.out.println("Received event for userId: " + event.getUserId() + ", amount: " + event.getAmount());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        final UUID sagaId = event.getSagaId();
        final Long orderId = event.getOrderId();
        final List<OrderItemDTO> items = event.getItems();
        final String idempotencyKey = event.getKeyIdempotence();

        Optional<ProcessedCommand> processedCommandOpt = processedCommandsRepository.findAllBySagaIdAndOrderId(sagaId, orderId).stream()
                .max(Comparator.comparing(ProcessedCommand::getId));

        // если команда уже была обработана, то ее пропускаем
        if (processedCommandOpt.isPresent() && processedCommandOpt.get().getIdempotencyKey().equals(idempotencyKey)) {
            return;
        }

        try {
            ProcessedCommand processedCommand = new ProcessedCommand();
            processedCommand.setSagaId(sagaId);
            processedCommand.setOrderId(orderId);
            processedCommand.setIdempotencyKey(idempotencyKey);
            processedCommandsRepository.save(processedCommand);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            inventoryService.reduceInventory(items);
        } catch (Exception e) {
            orderServiceProxy.sendInventoryReservedEvent(sagaId, orderId, false, "inventory for order is not reserved");
            return;
        }

        orderServiceProxy.sendInventoryReservedEvent(sagaId, orderId, true, "inventory for order reserved");
    }

    /**
     * Обработка приходящего сообщения по созданному заказу и поэтому требуется его обработать - снять деньги со счета.
     */
    @RabbitListener(queues = RabbitConfig.INVENTORY_RELEASE_COMMAND_QUEUE)
    public void handleReservePaymentCommand2(String messageBody) {
        ReleaseInventoryCommand event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, ReleaseInventoryCommand.class);
            System.out.println("Received event for userId: " + event.getUserId() + ", amount: " + event.getAmount());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        final UUID sagaId = event.getSagaId();
        final Long orderId = event.getOrderId();
        final List<OrderItemDTO> items = event.getItems();
        final String idempotencyKey = event.getKeyIdempotence();

        Optional<ProcessedCommand> processedCommandOpt = processedCommandsRepository.findAllBySagaIdAndOrderId(sagaId, orderId).stream()
                .max(Comparator.comparing(ProcessedCommand::getId));

        // если команда уже была обработана, то ее пропускаем
        if (processedCommandOpt.isPresent() && processedCommandOpt.get().getIdempotencyKey().equals(idempotencyKey)) {
            return;
        }

        try {
            ProcessedCommand processedCommand = new ProcessedCommand();
            processedCommand.setSagaId(sagaId);
            processedCommand.setOrderId(orderId);
            processedCommand.setIdempotencyKey(idempotencyKey);
            processedCommandsRepository.save(processedCommand);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            inventoryService.replenishInventory(items);
        } catch (Exception e) {
            orderServiceProxy.sendInventoryReleasedEvent(sagaId, orderId, false, "inventory for order is not released");
            return;
        }

        orderServiceProxy.sendInventoryReleasedEvent(sagaId, orderId, true, "inventory for order released");
    }

}
