package com.service.service;

import com.service.adapters.BillingAdapterService;
import com.service.adapters.RabbitAdapterService;
import com.service.adapters.events.OrderPaymentEvent;
import com.service.api.dto.OrderDTO;
import com.service.api.dto.OrderItemDTO;
import com.service.database.*;
import com.service.mapper.OrderMapper;
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

    private final RabbitAdapterService rabbitAdapterService;
    private final BillingAdapterService billingAdapterService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    /**
     * Получить заказы пользователя.
     */
    public List<OrderDTO> getOrderDTOs(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toOrderDTO)
                .toList();
    }

    /**
     * Создать заказ.
     */
    @Transactional
    public Optional<Order> createOrder(OrderDTO orderDTO) {
        Order newOrder = orderMapper.toOrder(orderDTO);

        int orderAmount = orderDTO.getItems().stream()
                .mapToInt(OrderItemDTO::getPrice)
                .sum();
        newOrder.setAmount(orderAmount);

        newOrder.setOrderStatus(OrderStatus.NEW);
        newOrder.setPaymentStatus(PaymentStatus.NOT_PAID);

        Order savedOrder = orderRepository.save(newOrder);

        boolean enoughMoneyForOrder = billingAdapterService.checkAccountAmount(orderDTO.getUserId(), savedOrder.getId().toString(), orderAmount);
        if (!enoughMoneyForOrder) {
            savedOrder.setOrderStatus(OrderStatus.CANCELLED);
            savedOrder.setPaymentStatus(PaymentStatus.NO_MONEY);
            rabbitAdapterService.sendOrderCancelledNoMoneyEvent(savedOrder);
        } else {
            savedOrder.setOrderStatus(OrderStatus.IN_PROCESS);
            savedOrder.setPaymentStatus(PaymentStatus.PENDING);
            rabbitAdapterService.sendOrderCreatedEvent(savedOrder);
        }

        Order inProcessOrder = orderRepository.save(savedOrder);
        return Optional.of(inProcessOrder);
    }

    /**
     * Обработать ивент о том, что заказ был оплачен успешно.
     */
    @Transactional
    public synchronized void handleOrderPaymentSuccessEvent(OrderPaymentEvent event) {
        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
        if (orderOpt.isEmpty()) {
            return;
        }
        Order order = orderOpt.get();
        PaymentStatus paymentStatusDB = order.getPaymentStatus();
        OrderStatus orderStatusDB = order.getOrderStatus();

        if (paymentStatusDB != PaymentStatus.PENDING || orderStatusDB != OrderStatus.IN_PROCESS) {
            return;
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        // нужно отправлять заказ на кухню готовиться после оплаты, но пока что будем считать если оплачен, значит уже готов
        order.setOrderStatus(OrderStatus.COMPLETED);

        System.out.println("saved: " + order);
        orderRepository.save(order);
        rabbitAdapterService.sendOrderCompletedEvent(order);
    }

    /**
     * Обработать ивент о том, что не хватило денег на оплату заказа.
     */
    @Transactional
    public synchronized void handleOrderNoMoneyEvent(OrderPaymentEvent event) {
        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
        if (orderOpt.isEmpty()) {
            return;
        }
        Order order = orderOpt.get();
        PaymentStatus paymentStatusDB = order.getPaymentStatus();
        OrderStatus orderStatusDB = order.getOrderStatus();

        if (paymentStatusDB != PaymentStatus.PENDING || orderStatusDB != OrderStatus.IN_PROCESS) {
            return;
        }

        order.setPaymentStatus(PaymentStatus.NO_MONEY);
        order.setOrderStatus(OrderStatus.CANCELLED);

        System.out.println("saved: " + order);
        orderRepository.save(order);
        rabbitAdapterService.sendOrderCancelledNoMoneyEvent(order);
    }

}
