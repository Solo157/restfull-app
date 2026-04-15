package com.service.api;

import com.service.database.OrderInfo;
import lombok.Data;

import java.util.*;

/**
 * DTO для получения пользователем информации по его уведомлениям.
 */
@Data
public class NotificationMessagesDTO {

    private String message;
    private Long orderId;
    private Integer amount;
    private List<OrderItemDTO> items;
    private String deliveryAddress;
    private String contactPhone;
    private String orderDate;

    public NotificationMessagesDTO(OrderInfo order) {
        this.message = order.getMessage();
        this.orderId = order.getOrderId();
        this.amount = order.getAmount();
        this.deliveryAddress = order.getDeliveryAddress();
        this.contactPhone = order.getContactPhone();
        this.orderDate = order.getOrderDate();

        this.items = order.getItemInfos().stream()
                .map(item -> new OrderItemDTO(item.getProductName(), item.getPrice()))
                .toList();
    }

}
