package com.service.adapters.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Ивент о том, что создался заказ и он требует оплату.
 */
@Data
public class OrderCreatedEvent {

    /**
     * Идентификатор пользователя.
     */
    private String userId;
    /**
     * Идентификатор закаа.
     */
    private Long orderId;
    /**
     * Сумма заказа.
     */
    private Integer amount;

    @JsonCreator
    public OrderCreatedEvent(@JsonProperty("userId") String userId,
                             @JsonProperty("orderId") Long orderId,
                             @JsonProperty("amount") Integer amount) {
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
    }

}
