package com.service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.events.OrderNotificationEvent;
import com.service.configuration.RabbitConfig;
import com.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Распределенный обработчик ивентов RabbitMQ.
 */
@Component
@RequiredArgsConstructor
public class DistributedEventHandler {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Обработка ивента о том, что заказ был успешно завершен.
     */
    @RabbitListener(queues = RabbitConfig.NOTIFICATION_ORDER_COMPLETED_QUEUE)
    public void handleOrderSucceededEvent(String messageBody) {
        try {
            System.out.println("Received message: " + messageBody);
            OrderNotificationEvent event = objectMapper.readValue(messageBody, OrderNotificationEvent.class);

            System.out.println("Processed OrderStatusEvent for userId: " + event.getUserId());
            notificationService.saveOrderInfoWithMessage(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
