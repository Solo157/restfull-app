package com.service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.events.PaymentReleasedEvent;
import com.service.adapter.events.PaymentReservedEvent;
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
    public void sendPaymentReservedEvent(UUID sagaId, Long orderId, boolean success, String message) {
        try {
            String payload = objectMapper.writeValueAsString(new PaymentReservedEvent(sagaId, orderId, success, message));

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, PAYMENT_RESERVED_EVENT_KEY, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправить ивент о том, что средства зачислены на счет.
     */
    public void sendPaymentReleasedEvent(UUID sagaId, Long orderId, boolean success, String message) {
        try {
            String payload = objectMapper.writeValueAsString(new PaymentReleasedEvent(sagaId, orderId, success, message));

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, PAYMENT_RELEASED_EVENT_KEY, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
