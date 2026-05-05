package com.service.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.commands.ReleaseDeliveryCommand;
import com.service.adapter.commands.ReserveDeliveryCommand;
import com.service.configuration.RabbitConfig;
import com.service.database.OrderItem;
import com.service.database.ProcessedCommand;
import com.service.database.ProcessedCommandsRepository;
import com.service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Обработчик команд из RabbitMQ для delivery-service.
 * Обрабатывает команды на назначение и снятие курьера для доставки заказа.
 */
@Component
@RequiredArgsConstructor
public class OrderServiceHandler {

    private final DeliveryService deliveryService;
    private final ProcessedCommandsRepository processedCommandsRepository;
    private final OrderServiceProxy orderServiceProxy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.DELIVERY_RESERVE_COMMAND_QUEUE)
    public void handleDeliveryReserveCommand(String messageBody) {
        System.out.println("Received message: " + messageBody);

        ReserveDeliveryCommand command = deserializeCommand(messageBody, ReserveDeliveryCommand.class);
        if (command == null) {
            return;
        }

        final UUID sagaId = command.getSagaId();
        final Long orderId = command.getOrderId();
        final List<OrderItemDTO> itemsDTOs = command.getItems();
        final String address = command.getAddress();
        final String idempotencyKey = command.getKeyIdempotence();

        if (!saveProcessedCommand(sagaId, orderId, idempotencyKey)) {
            orderServiceProxy.sendDeliveryReservedEvent(sagaId, orderId, false, "reserved command command already done");
            return;
        }

        boolean assignmentCourierToOrder = deliveryService.assignmentCourierToOrder(orderId, itemsDTOs, address);
        if (!assignmentCourierToOrder) {
            orderServiceProxy.sendDeliveryReservedEvent(sagaId, orderId, false, "courier is not assigment");
            return;
        }

        orderServiceProxy.sendDeliveryReservedEvent(sagaId, orderId, true, "courier is assigment");
    }

    @RabbitListener(queues = RabbitConfig.DELIVERY_RELEASE_COMMAND_QUEUE)
    public void handleDeliveryReleaseCommand(String messageBody) {
        System.out.println("Received message: " + messageBody);

        ReleaseDeliveryCommand command = deserializeCommand(messageBody, ReleaseDeliveryCommand.class);
        if (command == null) {
            return;
        }

        final UUID sagaId = command.getSagaId();
        final Long orderId = command.getOrderId();
        final String idempotencyKey = command.getKeyIdempotence();

        if (!saveProcessedCommand(sagaId, orderId, idempotencyKey)) {
            orderServiceProxy.sendDeliveryReleasedEvent(sagaId, orderId, false, "courier is not unassigment");

            return;
        }

        boolean unassignmentCourierToOrder = deliveryService.unassignmentCourierToOrder(orderId);
        if (!unassignmentCourierToOrder) {
            orderServiceProxy.sendDeliveryReleasedEvent(sagaId, orderId, false, "released command command already done");
            return;
        }

        orderServiceProxy.sendDeliveryReleasedEvent(sagaId, orderId, true, "courier is unassigment");
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
