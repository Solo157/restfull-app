package com.service.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.database.Order;
import com.service.database.OrderRepository;
import com.service.saga.KeyIdempotence;
import com.service.saga.OrderSagaState;
import com.service.saga.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.service.config.RabbitConfig.*;

@Component
@RequiredArgsConstructor
public class DeliveryServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Отправить команду на списание суммы по заказа с аккаунта пользователя.
     */
    public void sendReserveDeliveryCommand(OrderSagaState sagaState) {
        Long orderId = sagaState.getOrderId();
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return;
        }
        Order order = orderOpt.get();

        ReserveDeliveryCommand command = new ReserveDeliveryCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                order.getItems(),
                order.getDeliveryAddress(),
                UUID.randomUUID().toString()
        );

        try {
            String payload = objectMapper.writeValueAsString(command);

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, RESERVE_DELIVERY_COMMAND_KEY, payload);
            System.out.println("Message sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправить команду на возврат суммы по заказу на счет аккаунта пользователя.
     */
    public void sendReleaseDeliveryCommand(OrderSagaState sagaState) {
        ReleaseDeliveryCommand command = new ReleaseDeliveryCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                UUID.randomUUID().toString()
        );

        try {
            String payload = objectMapper.writeValueAsString(command);

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, RELEASE_DELIVERY_COMMAND_KEY, payload);
            System.out.println("Message sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
