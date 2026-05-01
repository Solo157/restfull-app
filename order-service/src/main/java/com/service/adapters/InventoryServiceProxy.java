package com.service.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.api.dto.OrderDTO;
import com.service.database.Order;
import com.service.database.OrderRepository;
import com.service.saga.KeyIdempotence;
import com.service.saga.OrderSagaState;
import com.service.saga.command.*;
import com.service.service.OrderManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.service.config.RabbitConfig.*;

@Component
@RequiredArgsConstructor
public class InventoryServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Отправить команду на списание суммы по заказа с аккаунта пользователя.
     */
    public void sendReserveInventoryCommand(OrderSagaState sagaState) {
        Optional<Order> orderOpt = orderRepository.findOrderById(sagaState.getOrderId())
                .stream()
                .filter(order -> order.getId().equals(sagaState.getOrderId()))
                .findFirst();
        if (orderOpt.isEmpty()) {
            return;
        }

        ReserveInventoryCommand command = new ReserveInventoryCommand();
        command.setSagaId(sagaState.getSagaId());
        command.setOrderId(sagaState.getOrderId());
        command.setUserId(sagaState.getUserId());
        command.setItems(orderOpt.get().getItems());
        command.setKeyIdempotence(KeyIdempotence.RESERVE.name());

        try {
            String payload = objectMapper.writeValueAsString(command);

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, RESERVE_INVENTORY_COMMAND_KEY, payload);
            System.out.println("Message sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправить команду на возврат суммы по заказу на счет аккаунта пользователя.
     */
    public void sendReleaseInventoryCommand(OrderSagaState sagaState) {
        Optional<Order> orderOpt = orderRepository.findOrderById(sagaState.getOrderId())
                .stream()
                .filter(order -> order.getId().equals(sagaState.getOrderId()))
                .findFirst();
        if (orderOpt.isEmpty()) {
            return;
        }

        ReleaseInventoryCommand command = new ReleaseInventoryCommand();
        command.setSagaId(sagaState.getSagaId());
        command.setOrderId(sagaState.getOrderId());
        command.setUserId(sagaState.getUserId());
        command.setItems(orderOpt.get().getItems());
        command.setKeyIdempotence(KeyIdempotence.RELEASE.name());

        try {
            String payload = objectMapper.writeValueAsString(command);

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, RELEASE_INVENTORY_COMMAND_KEY, payload);
            System.out.println("Message sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
