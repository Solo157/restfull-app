package com.service.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.database.Order;
import com.service.database.OrderRepository;
import com.service.saga.KeyIdempotence;
import com.service.saga.OrderSagaState;
import com.service.saga.command.ReleasePaymentCommand;
import com.service.saga.command.ReservePaymentCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.service.config.RabbitConfig.RELEASE_PAYMENT_COMMAND_KEY;
import static com.service.config.RabbitConfig.RESERVE_PAYMENT_COMMAND_KEY;
import static com.service.config.RabbitConfig.ORDER_EVENTS_TOPIC_EXCHANGE;

@Component
@RequiredArgsConstructor
public class BillingServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Отправить команду на списание суммы по заказа с аккаунта пользователя.
     */
    public void sendReservePaymentCommand(OrderSagaState sagaState) {
        Long orderId = sagaState.getOrderId();
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return;
        }
        Order order = orderOpt.get();

        ReservePaymentCommand command = new ReservePaymentCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                order.getAmount(),
                UUID.randomUUID().toString()
        );

        try {
            String payload = objectMapper.writeValueAsString(command);

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, RESERVE_PAYMENT_COMMAND_KEY, payload);
            System.out.println("Message sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправить команду на возврат суммы по заказу на счет аккаунта пользователя.
     */
    public void sendReleasePaymentCommand(OrderSagaState sagaState) {
        Long orderId = sagaState.getOrderId();
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return;
        }
        Order order = orderOpt.get();

        ReleasePaymentCommand command = new ReleasePaymentCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                order.getAmount(),
                UUID.randomUUID().toString()
        );

        try {
            String payload = objectMapper.writeValueAsString(command);

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, RELEASE_PAYMENT_COMMAND_KEY, payload);
            System.out.println("Message sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
