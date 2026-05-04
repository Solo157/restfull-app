package com.service.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.events.PaymentReleasedEvent;
import com.service.adapter.events.PaymentReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.service.configuration.RabbitConfig.*;

/**
 * Прокси для отправки событий в order-service через RabbitMQ.
 * Отправляет события о результате резервирования и возврата средств.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendPaymentReservedEvent(UUID sagaId, Long orderId, boolean success, String message) {
        sendMessage(PAYMENT_RESERVED_EVENT_KEY, new PaymentReservedEvent(sagaId, orderId, success, message));
    }

    public void sendPaymentReleasedEvent(UUID sagaId, Long orderId, boolean success, String message) {
        sendMessage(PAYMENT_RELEASED_EVENT_KEY, new PaymentReleasedEvent(sagaId, orderId, success, message));
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
