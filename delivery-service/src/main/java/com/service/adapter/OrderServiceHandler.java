package com.service.adapter;

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

import java.util.*;

/**
 * Обработчик сообщений от RabbitMQ.
 */
@Component
@RequiredArgsConstructor
public class OrderServiceHandler {

    private final DeliveryService deliveryService;
    private final ProcessedCommandsRepository processedCommandsRepository;
    private final OrderServiceProxy orderServiceProxy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Обработка приходящего сообщения по созданному заказу и поэтому требуется его обработать - снять деньги со счета.
     */
    @RabbitListener(queues = RabbitConfig.DELIVERY_RESERVE_COMMAND_QUEUE)
    public void handleDeliveryReserveCommand(String messageBody) {
        ReserveDeliveryCommand event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, ReserveDeliveryCommand.class);
            System.out.println("Received event for userId: " + event.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        final UUID sagaId = event.getSagaId();
        final Long orderId = event.getOrderId();
        final List<OrderItem> items = event.getItems().stream()
                .map(itemDTO -> new OrderItem(itemDTO.getProductName(), itemDTO.getPrice(), itemDTO.getCount()))
                .toList();
        final String address = event.getAddress();
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

        boolean assignmentCourierToOrder = deliveryService.assignmentCourierToOrder(orderId, items, address);
        if (!assignmentCourierToOrder) {
            orderServiceProxy.sendDeliveryReservedEvent(sagaId, orderId, false, "courier is not assigment");
            return;
        }

        orderServiceProxy.sendDeliveryReservedEvent(sagaId, orderId, true, "courier is assigment");
    }

    /**
     * Обработка приходящего сообщения по созданному заказу и поэтому требуется его обработать - снять деньги со счета.
     */
    @RabbitListener(queues = RabbitConfig.DELIVERY_RELEASE_COMMAND_QUEUE)
    public void handleReservePaymentCommand2(String messageBody) {
        ReleaseDeliveryCommand event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, ReleaseDeliveryCommand.class);
            System.out.println("Received event for userId: " + event.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        final UUID sagaId = event.getSagaId();
        final Long orderId = event.getOrderId();
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

        boolean unassignmentCourierToOrder = deliveryService.unassignmentCourierToOrder(orderId);
        if (!unassignmentCourierToOrder) {
            orderServiceProxy.sendDeliveryReleasedEvent(sagaId, orderId, false, "courier is not unassigment");
            return;
        }

        orderServiceProxy.sendDeliveryReleasedEvent(sagaId, orderId, true, "courier is unassigment");
    }

}
