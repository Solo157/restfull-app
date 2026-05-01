package com.service.service;

import com.service.adapters.BillingServiceProxy;
import com.service.adapters.NotificationServiceProxy;
import com.service.api.dto.OrderDTO;
import com.service.api.dto.OrderItemDTO;
import com.service.database.*;
import com.service.mapper.OrderMapper;
import com.service.saga.OrderSagaState;
import com.service.saga.SagaCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Главный класс сервис по работе с ордерами.
 */
@Service
@RequiredArgsConstructor
public class OrderManagerService {

    private final BillingServiceProxy billingServiceProxy;
    private final SagaCoordinator sagaCoordinator;
    private final OrderRepository orderRepository;
    private final NotificationServiceProxy notificationServiceProxy;
    private final OrderMapper orderMapper;

    /**
     * Получить заказы пользователя.
     */
    public List<OrderDTO> getOrderDTOs(String userId) {
        return orderRepository.findOrderByUserId(userId).stream()
                .map(orderMapper::toOrderDTO)
                .toList();
    }

    @Transactional
    public Optional<Order> createOrder(OrderDTO orderDTO) {
        Order newOrder = orderMapper.toOrder(orderDTO);

        int orderAmount = orderDTO.getItems().stream()
                .mapToInt(OrderItemDTO::getPrice)
                .sum();
        newOrder.setAmount(orderAmount);

        newOrder.setOrderStatus(OrderStatus.NEW);

        Order savedOrder = orderRepository.save(newOrder);

        UUID sagaId = UUID.randomUUID();

        OrderSagaState startSagaState = sagaCoordinator.getStartSagaState(
                sagaId,
                savedOrder.getId(),
                orderDTO.getUserId(),
                orderAmount
        );

        billingServiceProxy.sendReservePaymentCommand(startSagaState);
        return Optional.of(savedOrder);
    }

    @Transactional
    public void completeOrder(Long orderId, String statusMessage) {
        changeStatusAndNotify(orderId, statusMessage, OrderStatus.COMPLETED);
    }

    @Transactional
    public void cancelOrder(Long orderId, String statusMessage) {
        changeStatusAndNotify(orderId, statusMessage, OrderStatus.CANCELLED);
    }

    private void changeStatusAndNotify(Long orderId, String statusMessage, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        orderOpt.ifPresent(order -> {
            order.setOrderStatus(status);
//            order.setStatusMessage(statusMessage);
            orderRepository.save(order);
            notificationServiceProxy.sendOrderCompletedEvent(order, statusMessage);
        });
    }

}
