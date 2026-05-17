package com.service.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.events.OrderNotificationEvent;
import com.service.configuration.RabbitConfig;
import com.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Обработчик событий из RabbitMQ для notification-service.
 * Принимает события о завершении заказа и сохраняет информацию для отправки уведомлений.
 */
@Component
@RequiredArgsConstructor
public class DistributedEventHandler {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_ORDER_COMPLETED_QUEUE)
    public void handleOrderSucceededEvent(String messageBody) {
        OrderNotificationEvent event = deserializeEvent(messageBody);
        if (event == null) {
            return;
        }

        System.out.println("Processed OrderStatusEvent for userId: " + event.getUserId());
        notificationService.saveOrderInfoWithMessage(event);
    }

    private OrderNotificationEvent deserializeEvent(String messageBody) {
        System.out.println("Received message: " + messageBody);
        try {
            return objectMapper.readValue(messageBody, OrderNotificationEvent.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
