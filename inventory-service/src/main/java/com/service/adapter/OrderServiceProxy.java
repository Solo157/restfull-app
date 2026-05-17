package com.service.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.events.InventoryReleasedEvent;
import com.service.adapter.events.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.service.configuration.RabbitConfig.INVENTORY_RELEASED_EVENT_KEY;
import static com.service.configuration.RabbitConfig.INVENTORY_RESERVED_EVENT_KEY;
import static com.service.configuration.RabbitConfig.ORDER_EVENTS_TOPIC_EXCHANGE;

/**
 * Прокси для отправки событий в order-service через RabbitMQ.
 * Отправляет события о результате резервирования и освобождения товаров.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendInventoryReservedEvent(UUID sagaId, Long orderId, boolean success, String message) {
        sendMessage(INVENTORY_RESERVED_EVENT_KEY, new InventoryReservedEvent(sagaId, orderId, success, message));
    }

    public void sendInventoryReleasedEvent(UUID sagaId, Long orderId, boolean success, String message) {
        sendMessage(INVENTORY_RELEASED_EVENT_KEY, new InventoryReleasedEvent(sagaId, orderId, success, message));
    }

    private void sendMessage(String routingKey, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, routingKey, payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
