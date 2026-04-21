package com.service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapter.events.OrderPaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.service.configuration.RabbitConfig.ORDER_EVENTS_TOPIC_EXCHANGE;
import static com.service.configuration.RabbitConfig.PAYMENT_NO_MONEY_KEY;
import static com.service.configuration.RabbitConfig.PAYMENT_SUCCEEDED_KEY;

/**
 * Распределенный адаптер по отправке сообщений. Работает на базе RabbitMQ.
 */
@Service
@RequiredArgsConstructor
public class DistributedAdapterSender {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Отправить ивент том, что списание денег за заказ было успешным.
     */
    public void sendOrderPaymentSucceededEvent(String userId, Long orderId) {
        try {
            String payload = objectMapper.writeValueAsString(new OrderPaymentEvent(userId, orderId));

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, PAYMENT_SUCCEEDED_KEY, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправить ивент о том, что денег для оплаты заказа не хватило.
     */
    public void sendOrderPaymentNoMoneyEvent(String userId, Long orderId) {
        try {
            String payload = objectMapper.writeValueAsString(new OrderPaymentEvent(userId, orderId));

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, PAYMENT_NO_MONEY_KEY, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
