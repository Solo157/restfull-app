package com.service.service;

import com.service.adapter.events.OrderNotificationEvent;
import com.service.api.NotificationMessagesDTO;
import com.service.database.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Главный класс сервис по работе с уведомлениями.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Получить уведомления пользователя.
     */
    @Transactional
    public List<NotificationMessagesDTO> getUserNotificationMessages(String userId) {
        return getOrCreateNotification(userId).getOrders().stream()
                .map(NotificationMessagesDTO::new)
                .toList();
    }

    /**
     * Создать уведомление.
     */
    public Notification getOrCreateNotification(String userId) {
        return notificationRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Notification newNotification = new Notification();
                    newNotification.setUserId(userId);
                    newNotification.setOrders(new ArrayList<>());
                    return notificationRepository.save(newNotification);
                });
    }

    @Transactional
    public void saveOrderInfoWithMessage(OrderNotificationEvent event) {
        Notification notification = getOrCreateNotification(event.getUserId());

        OrderInfo order = new OrderInfo();
        order.setMessage(event.getMessage());
        order.setOrderId(event.getOrderId());
        order.setNotification(notification);
        order.setAmount(event.getAmount());

        List<OrderItemInfo> orderItemInfos = event.getItems().stream()
                .map(item -> new OrderItemInfo(item.getProductName(), item.getPrice(), item.getCount()))
                .toList();
        order.setItemInfos(orderItemInfos);

        order.setDeliveryAddress(event.getDeliveryAddress());
        order.setContactPhone(event.getContactPhone());
        order.setOrderDate(event.getOrderDate());

        notification.getOrders().add(order);
        notificationRepository.save(notification);
    }

}
