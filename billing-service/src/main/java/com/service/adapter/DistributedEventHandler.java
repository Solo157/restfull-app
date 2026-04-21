package com.service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.events.OrderCreatedEvent;
import com.service.configuration.RabbitConfig;
import com.service.service.BillingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Обработчик сообщений от RabbitMQ.
 */
@Component
@RequiredArgsConstructor
public class DistributedEventHandler {

    private final BillingAccountService billingAccountService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Обработка приходящего сообщения по созданному заказу и поэтому требуется его обработать - снять деньги со счета.
     */
    @RabbitListener(queues = RabbitConfig.BILLING_ORDER_CREATED_QUEUE)
    public void handleOrderCreatedEvent(String messageBody) {
        try {
            System.out.println("Received message: " + messageBody);
            OrderCreatedEvent event = objectMapper.readValue(messageBody, OrderCreatedEvent.class);

            System.out.println("Received event for userId: " + event.getUserId() + ", amount: " + event.getAmount());
            billingAccountService.handleOrderCreatedEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
