package com.service.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.commands.ReleasePaymentCommand;
import com.service.adapter.commands.ReservePaymentCommand;
import com.service.configuration.RabbitConfig;
import com.service.database.ProcessedCommand;
import com.service.database.ProcessedCommandsRepository;
import com.service.service.BillingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Обработчик команд из RabbitMQ от order-service.
 * Обрабатывает команды на резервирование и возврат средств пользователя.
 */
@Component
@RequiredArgsConstructor
public class OrderServiceHandler {

    private final BillingAccountService billingAccountService;
    private final ProcessedCommandsRepository processedCommandsRepository;
    private final OrderServiceProxy orderServiceProxy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.BILLING_RESERVE_PAYMENT_COMMAND_QUEUE)
    public void handleReservePaymentCommand(String messageBody) {
        System.out.println("Received message: " + messageBody);

        ReservePaymentCommand command = deserializeCommand(messageBody, ReservePaymentCommand.class);
        if (command == null) {
            return;
        }

        final UUID sagaId = command.getSagaId();
        final Long orderId = command.getOrderId();
        final String userId = command.getUserId();
        final Integer amount = command.getAmount();
        final String idempotencyKey = command.getKeyIdempotence();

        if (!saveProcessedCommand(sagaId, orderId, idempotencyKey)) {
            orderServiceProxy.sendPaymentReservedEvent(sagaId, orderId, false, "reserved command command already done");
            return;
        }

        boolean withdrawAccount = billingAccountService.withdrawAccount(userId, amount);
        String message = withdrawAccount ? "cash for order reserved" : "cash for order is not reserved";
        orderServiceProxy.sendPaymentReservedEvent(sagaId, orderId, withdrawAccount, message);
    }

    @RabbitListener(queues = RabbitConfig.BILLING_RELEASE_PAYMENT_COMMAND_QUEUE)
    public void handleReleasePaymentCommand(String messageBody) {
        System.out.println("Received message: " + messageBody);

        ReleasePaymentCommand command = deserializeCommand(messageBody, ReleasePaymentCommand.class);
        if (command == null) {
            return;
        }

        final UUID sagaId = command.getSagaId();
        final Long orderId = command.getOrderId();
        final String userId = command.getUserId();
        final Integer amount = command.getAmount();
        final String idempotencyKey = command.getKeyIdempotence();

        if (!saveProcessedCommand(sagaId, orderId, idempotencyKey)) {
            orderServiceProxy.sendPaymentReleasedEvent(sagaId, orderId, false, "released command already done");
            return;
        }

        boolean depositAccount = billingAccountService.depositAccount(userId, amount);
        String message = depositAccount ? "cash for order released" : "cash for order is not released";
        orderServiceProxy.sendPaymentReleasedEvent(sagaId, orderId, depositAccount, message);
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
