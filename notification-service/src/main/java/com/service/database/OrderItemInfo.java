package com.service.database;

import jakarta.persistence.Embeddable;
import lombok.Data;

/**
 * Информация о пункте заказа.
 */
@Data
@Embeddable
public class OrderItemInfo {

    /**
     * Название пункта меню.
     */
    private String productName;
    /**
     * Стоимость за 1 единицу.
     */
    private Integer price;

    private Integer count;

    public OrderItemInfo() {
    }

    public OrderItemInfo(String productName, Integer price, Integer count) {
        this.productName = productName;
        this.price = price;
        this.count = count;
    }

}
