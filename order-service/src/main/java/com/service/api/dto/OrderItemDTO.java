package com.service.api.dto;

import lombok.Data;

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

    public OrderItemDTO() {
    }

    public OrderItemDTO(String productName, Integer price) {
        this.productName = productName;
        this.price = price;
    }

}
