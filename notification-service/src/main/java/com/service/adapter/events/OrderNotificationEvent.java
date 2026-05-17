package com.service.adapter.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.api.OrderItemDTO;
import lombok.Data;

import java.util.*;

/**
 * Ивент о смене статусов ордера.
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

    /**
     * Текст уведомления, описывающий итоговое сообщение для пользователя.
     */
    private String message;

    /**
     * Сумма заказа.
     */
    private Integer amount;

    @JsonCreator
    public OrderNotificationEvent(@JsonProperty("userId") String userId,
                                  @JsonProperty("orderId") Long orderId,
                                  @JsonProperty("items") List<OrderItemDTO> items,
                                  @JsonProperty("deliveryAddress") String deliveryAddress,
                                  @JsonProperty("contactPhone") String contactPhone,
                                  @JsonProperty("orderDate") String orderDate,
                                  @JsonProperty("message") String message,
                                  @JsonProperty("amount") Integer amount) {
        this.userId = userId;
        this.orderId = orderId;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.contactPhone = contactPhone;
        this.orderDate = orderDate;
        this.message = message;
        this.amount = amount;
    }

}
