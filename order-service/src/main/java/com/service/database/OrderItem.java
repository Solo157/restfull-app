package com.service.database;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class OrderItem {

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

    public OrderItem() {
    }

    public OrderItem(String productName, Integer price) {
        this.productName = productName;
        this.price = price;
    }

}
