package com.service.api;

import lombok.Data;

/**
 * DTO товаров заказа.
 */
@Data
public class OrderItemDTO {

    /**
     * Название пункта меню.
     */
    private String productName;
    /**
     * Стоимость за 1 единицу.
     */
    private Integer price;

    private Integer count;

    public OrderItemDTO() {
    }

    public OrderItemDTO(String productName, Integer price, Integer count) {
        this.productName = productName;
        this.price = price;
        this.count = count;
    }

}
