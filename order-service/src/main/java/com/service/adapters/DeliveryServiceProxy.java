package com.service.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.database.Order;
import com.service.database.OrderRepository;
import com.service.database.OrderSagaState;
import com.service.saga.command.ReleaseDeliveryCommand;
import com.service.saga.command.ReserveDeliveryCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.service.config.RabbitConfig.*;

/**
 * Прокси для отправки команд в delivery-service через RabbitMQ.
 * Отправляет команды на назначение и снятие курьера для доставки заказа.
 */
@Component
@RequiredArgsConstructor
public class DeliveryServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendReserveDeliveryCommand(OrderSagaState sagaState) {
        Order order = findOrder(sagaState.getOrderId());
        if (order == null) {
            return;
        }

        ReserveDeliveryCommand command = new ReserveDeliveryCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                order.getItems(),
                order.getDeliveryAddress(),
                UUID.randomUUID().toString()
        );

        sendMessage(RESERVE_DELIVERY_COMMAND_KEY, command);
    }

    public void sendReleaseDeliveryCommand(OrderSagaState sagaState) {
        ReleaseDeliveryCommand command = new ReleaseDeliveryCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                UUID.randomUUID().toString()
        );

        sendMessage(RELEASE_DELIVERY_COMMAND_KEY, command);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    private void sendMessage(String routingKey, Object command) {
        try {
            String payload = objectMapper.writeValueAsString(command);
            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, routingKey, payload);
            System.out.println("Message sent");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
