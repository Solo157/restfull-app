package com.service.service;

import com.service.adapters.BillingAdapterService;
import com.service.adapters.OrderStatusEventDTO;
import com.service.adapters.RabbitAdapterService;
import com.service.database.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderManagerService {

    private final RabbitAdapterService rabbitAdapterService;
    private final BillingAdapterService billingAdapterService;
    private final OrderRepository orderRepository;

    public Optional<Order> createOrder(String userId, Long orderAmount) {
        Order order = new Order();
        order.setUserId(userId);
        order.setAmount(orderAmount);
        order.setOrderStatus(OrderStatus.NEW);
        order.setPaymentStatus(PaymentStatus.NOT_PAID);

        Order savedOrder = orderRepository.save(order);

        boolean isAccountAmountOk = billingAdapterService.checkAccountAmount(userId, savedOrder.getId().toString(), orderAmount);
        if (!isAccountAmountOk) {
            return Optional.empty();
        }

        // готовим заказ...
        savedOrder.setOrderStatus(OrderStatus.IN_PROCESS);
        savedOrder.setPaymentStatus(PaymentStatus.PENDING);

        rabbitAdapterService.sendOrderStatusEvent(savedOrder);
        return Optional.of(savedOrder);
    }

    public void handleOrderStatusEvent(OrderStatusEventDTO event) {
        PaymentStatus paymentStatus = event.getPaymentStatus();
        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
        if (orderOpt.isEmpty()) {
            return;
        }
        Order order = orderOpt.get();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();
        if (oldPaymentStatus != PaymentStatus.PENDING) {
            return;
        }

        if (paymentStatus == PaymentStatus.PAID) {
            order.setPaymentStatus(paymentStatus);
            order.setOrderStatus(OrderStatus.COMPLETED);
        }
        if (paymentStatus == PaymentStatus.NOT_PAID) {
            order.setPaymentStatus(paymentStatus);
        }

        orderRepository.save(order);
        rabbitAdapterService.sendOrderStatusEvent(order);
    }

}
