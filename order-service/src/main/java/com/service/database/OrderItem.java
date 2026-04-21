package com.service.database;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class OrderItem {

    /**
     * Название пункта меню.
     */
    private String productName;
    /**
     * Стоимость за 1 единицу.
     */
    private Integer price;

    public OrderItem() {
    }

    public OrderItem(String productName, Integer price) {
        this.productName = productName;
        this.price = price;
    }

}
