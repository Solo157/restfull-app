package com.service.api.dto;

import lombok.Data;

import java.util.*;

@Data
public class OrderDTO {

    /**
     * Идентификатор пользователя.
     */
    private String userId;
    /**
     * Список пунктов/товаров заказа.
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
     * Версия заказа.
     */
    private Long version;

}
