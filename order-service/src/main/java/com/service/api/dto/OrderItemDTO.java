package com.service.api.dto;

import lombok.Data;

@Data
public class OrderItemDTO {

    /**
     * Название пункта заказа.
     */
    private String productName;
    /**
     * Стоимость за 1 единицу.
     */
    private Integer price;
    /**
     * Количество.
     */
    private Integer count;

    public OrderItemDTO() {
    }

    public OrderItemDTO(String productName, Integer price, Integer count) {
        this.productName = productName;
        this.price = price;
        this.count = count;
    }

}
