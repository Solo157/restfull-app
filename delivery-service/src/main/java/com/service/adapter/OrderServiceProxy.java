package com.service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.events.DeliveryReleasedEvent;
import com.service.adapter.events.DeliveryReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.service.configuration.RabbitConfig.*;

/**
 * Распределенный адаптер по отправке сообщений. Работает на базе RabbitMQ.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Отправить ивент том, что средства списаны со счета.
     */
    public void sendDeliveryReservedEvent(UUID sagaId, Long orderId, boolean success, String message) {
        try {
            String payload = objectMapper.writeValueAsString(new DeliveryReservedEvent(sagaId, orderId, success, message));

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, DELIVERY_RESERVED_EVENT_KEY, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправить ивент о том, что средства зачислены на счет.
     */
    public void sendDeliveryReleasedEvent(UUID sagaId, Long orderId, boolean success, String message) {
        try {
            String payload = objectMapper.writeValueAsString(new DeliveryReleasedEvent(sagaId, orderId, success, message));

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, DELIVERY_RELEASED_EVENT_KEY, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
