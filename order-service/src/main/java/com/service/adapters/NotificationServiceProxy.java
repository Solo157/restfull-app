package com.service.adapters;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapters.events.OrderNotificationEvent;
import com.service.database.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.service.config.RabbitConfig.ORDER_COMPLETED_KEY;
import static com.service.config.RabbitConfig.ORDER_EVENTS_TOPIC_EXCHANGE;

/**
 * Прокси для отправки событий в notification-service через RabbitMQ.
 * Отправляет уведомления о завершении и отмене заказов.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendOrderCompletedEvent(Order order, String message) {
        OrderNotificationEvent event = new OrderNotificationEvent(
                order.getUserId(),
                order.getId(),
                order.getItems(),
                order.getDeliveryAddress(),
                order.getContactPhone(),
                order.getOrderDate(),
                message,
                order.getAmount()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, ORDER_COMPLETED_KEY, payload);
            System.out.println("Message sent: " + message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
