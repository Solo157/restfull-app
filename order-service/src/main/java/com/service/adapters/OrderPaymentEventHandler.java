package com.service.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapters.events.OrderPaymentEvent;
import com.service.config.RabbitConfig;
import com.service.service.OrderManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Обработчик ивентов из billing сервиса.
 */
@Component
@RequiredArgsConstructor
public class OrderPaymentEventHandler {

    private final OrderManagerService orderManagerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Обработка ивента о том, что не хватило денег на оплату заказа.
     */
    @RabbitListener(queues = RabbitConfig.ORDER_PAYMENT_NO_MONEY_QUEUE)
    public void handleOrderPaymentNoMoneyMessage(String messageBody) {
        try {
            System.out.println("Received message: " + messageBody);
            OrderPaymentEvent event = objectMapper.readValue(messageBody, OrderPaymentEvent.class);

            System.out.println("Received event for userId: " + event.getUserId());
            orderManagerService.handleOrderNoMoneyEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработка ивента о том, что оплата прошла успешно за заказ. Деньги списались со счета аккаунта.
     */
    @RabbitListener(queues = RabbitConfig.ORDER_PAYMENT_SUCCEEDED_QUEUE)
    public void handleOrderPaymentSucceededMessage(String messageBody) {
        try {
            System.out.println("Received message: " + messageBody);
            OrderPaymentEvent event = objectMapper.readValue(messageBody, OrderPaymentEvent.class);

            System.out.println("Received event for userId: " + event.getUserId());
            orderManagerService.handleOrderPaymentSuccessEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
