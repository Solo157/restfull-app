package com.service.adapters.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.api.dto.OrderItemDTO;
import com.service.database.OrderItem;
import lombok.Data;

import java.util.*;

/**
 * Ивент об уведомлениях заказа. Нужен для отправки информации по заказу в сервис нотификаций.
 */
@Data
public class OrderNotificationEvent {

    /**
     * Идентификатор пользователя.
     */
    private String userId;
    /**
     * Идентификатор заказа.
     */
    private Long orderId;
    /**
     * Список пунктов заказа.
     */
    private List<OrderItemDTO> items;
    /**
     * Адрес доставки.
     */
    private String deliveryAddress;
    /**
     * Контактный телефон.
     */
    private String contactPhone;
    /**
     * Дата заказа.
     */
    private String orderDate;

    private String message;

    /**
     * Сумма заказа.
     */
    private Integer amount;

    @JsonCreator
    public OrderNotificationEvent(@JsonProperty("userId") String userId,
                                  @JsonProperty("orderId") Long orderId,
                                  @JsonProperty("items") List<OrderItem> items,
                                  @JsonProperty("deliveryAddress") String deliveryAddress,
                                  @JsonProperty("contactPhone") String contactPhone,
                                  @JsonProperty("orderDate") String orderDate,
                                  @JsonProperty("message") String message,
                                  @JsonProperty("amount") Integer amount) {
        this.userId = userId;
        this.orderId = orderId;
        this.deliveryAddress = deliveryAddress;
        this.contactPhone = contactPhone;
        this.orderDate = orderDate;
        this.message = message;
        this.amount = amount;

        this.items = items.stream()
                .map(item -> new OrderItemDTO(item.getProductName(), item.getPrice(), item.getCount()))
                .toList();
    }

}
