package com.service.adapter;

import lombok.Data;

/**
 * Опредленный пункт заказа.
 */
@Data
public class OrderItem {

    /**
     * Название пункта меню.
     */
    private String productName;
    /**
     * Стоимость за 1 единицу.
     */
    private Integer price;

    private Integer count;

    public OrderItem() {
    }

    public OrderItem(String productName, Integer price, Integer count) {
        this.productName = productName;
        this.price = price;
        this.count = count;
    }

}
