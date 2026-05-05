package com.service.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.database.Order;
import com.service.database.OrderRepository;
import com.service.database.OrderSagaState;
import com.service.saga.command.ReleasePaymentCommand;
import com.service.saga.command.ReservePaymentCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.service.config.RabbitConfig.ORDER_EVENTS_TOPIC_EXCHANGE;
import static com.service.config.RabbitConfig.RELEASE_PAYMENT_COMMAND_KEY;
import static com.service.config.RabbitConfig.RESERVE_PAYMENT_COMMAND_KEY;

/**
 * Прокси для отправки команд в billing-service через RabbitMQ.
 * Отправляет команды на резервирование и возврат средств.
 */
@Component
@RequiredArgsConstructor
public class BillingServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendReservePaymentCommand(OrderSagaState sagaState) {
        Order order = findOrder(sagaState.getOrderId());
        if (order == null) {
            return;
        }

        ReservePaymentCommand command = new ReservePaymentCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                order.getAmount(),
                UUID.randomUUID().toString()
        );

        sendMessage(RESERVE_PAYMENT_COMMAND_KEY, command);
    }

    public void sendReleasePaymentCommand(OrderSagaState sagaState) {
        Order order = findOrder(sagaState.getOrderId());
        if (order == null) {
            return;
        }

        ReleasePaymentCommand command = new ReleasePaymentCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                order.getAmount(),
                UUID.randomUUID().toString()
        );

        sendMessage(RELEASE_PAYMENT_COMMAND_KEY, command);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    private void sendMessage(String routingKey, Object command) {
        System.out.println("Message sent" + command);

        try {
            String payload = objectMapper.writeValueAsString(command);
            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, routingKey, payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
