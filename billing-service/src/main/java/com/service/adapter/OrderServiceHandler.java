package com.service.adapter;

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

import java.util.*;

/**
 * Обработчик сообщений от RabbitMQ.
 */
@Component
@RequiredArgsConstructor
public class OrderServiceHandler {

    private final BillingAccountService billingAccountService;
    private final ProcessedCommandsRepository processedCommandsRepository;
    private final OrderServiceProxy orderServiceProxy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Обработка приходящего сообщения по созданному заказу и поэтому требуется его обработать - снять деньги со счета.
     */
    @RabbitListener(queues = RabbitConfig.BILLING_RESERVE_PAYMENT_COMMAND_QUEUE)
    public void handleReservePaymentCommand(String messageBody) {
        ReservePaymentCommand event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, ReservePaymentCommand.class);
            System.out.println("Received event for userId: " + event.getUserId() + ", amount: " + event.getAmount());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        final UUID sagaId = event.getSagaId();
        final Long orderId = event.getOrderId();
        final String userId = event.getUserId();
        final Integer amount = event.getAmount();
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

        boolean withdrawAccount = billingAccountService.withdrawAccount(userId, amount);

        String message = withdrawAccount ? "cash for order reserved" : "cash for order is not reserved";
        orderServiceProxy.sendPaymentReservedEvent(sagaId, orderId, withdrawAccount, message);
    }

    /**
     * Обработка приходящего сообщения по созданному заказу и поэтому требуется его обработать - снять деньги со счета.
     */
    @RabbitListener(queues = RabbitConfig.BILLING_RELEASE_PAYMENT_COMMAND_QUEUE)
    public void handleReservePaymentCommand2(String messageBody) {
        ReleasePaymentCommand event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, ReleasePaymentCommand.class);
            System.out.println("Received event for userId: " + event.getUserId() + ", amount: " + event.getAmount());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        final UUID sagaId = event.getSagaId();
        final Long orderId = event.getOrderId();
        final String userId = event.getUserId();
        final Integer amount = event.getAmount();
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

        boolean depositAccount = billingAccountService.depositAccount(userId, amount);

        String message = depositAccount ? "cash for order released" : "cash for order is not released";
        orderServiceProxy.sendPaymentReservedEvent(sagaId, orderId, depositAccount, message);
    }

}
