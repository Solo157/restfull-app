package com.service.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.database.Order;
import com.service.database.OrderRepository;
import com.service.database.OrderSagaState;
import com.service.saga.command.ReleaseInventoryCommand;
import com.service.saga.command.ReserveInventoryCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.service.config.RabbitConfig.*;

/**
 * Прокси для отправки команд в inventory-service через RabbitMQ.
 * Отправляет команды на резервирование и освобождение товаров на складе.
 */
@Component
@RequiredArgsConstructor
public class InventoryServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendReserveInventoryCommand(OrderSagaState sagaState) {
        Order order = findOrder(sagaState.getOrderId());
        if (order == null) {
            return;
        }

        ReserveInventoryCommand command = new ReserveInventoryCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                order.getItems(),
                UUID.randomUUID().toString()
        );

        sendMessage(RESERVE_INVENTORY_COMMAND_KEY, command);
    }

    public void sendReleaseInventoryCommand(OrderSagaState sagaState) {
        Order order = findOrder(sagaState.getOrderId());
        if (order == null) {
            return;
        }

        ReleaseInventoryCommand command = new ReleaseInventoryCommand(
                sagaState.getSagaId(),
                sagaState.getOrderId(),
                sagaState.getUserId(),
                order.getItems(),
                UUID.randomUUID().toString()
        );

        sendMessage(RELEASE_INVENTORY_COMMAND_KEY, command);
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
