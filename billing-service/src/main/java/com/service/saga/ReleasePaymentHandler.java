package com.service.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.events.OrderCreatedEvent;
import com.service.configuration.RabbitConfig;
import com.service.database.ProcessedCommand;
import com.service.database.ProcessedCommandsRepository;
import com.service.service.BillingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
public class ReleasePaymentHandler {

    private final BillingAccountService billingAccountService;
    private final ProcessedCommandsRepository processedCommandsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Обработка приходящего сообщения по созданному заказу и поэтому требуется его обработать - снять деньги со счета.
     */
    @RabbitListener(queues = RabbitConfig.BILLING_ORDER_CREATED_QUEUE)
    public void handleOrderCreatedEvent(String messageBody) {
        PaymentReserveEvent event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, PaymentReserveEvent.class);
            System.out.println("Received event for userId: " + event.getUserId() + ", amount: " + event.getAmount());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        UUID sagaId = event.getSagaId();
        Long orderId = event.getOrderId();
        Optional<ProcessedCommand> processedCommandOpt = processedCommandsRepository.findAllBySagaIdAndOrderId(sagaId, orderId).stream()
                .max(Comparator.comparing(ProcessedCommand::getId));

        if (processedCommandOpt.isEmpty()) {
            return;
        }

        ProcessedCommand processedCommand = processedCommandOpt.get();
        KeyIdempotence keyIdempotence = processedCommand.getKeyIdempotence();

        if (keyIdempotence != KeyIdempotence.RESERVE) {
            return;
        }


        billingAccountService.handleOrderCreatedEvent(event);
    }

}
