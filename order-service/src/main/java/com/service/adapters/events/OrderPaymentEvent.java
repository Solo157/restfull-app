package com.service.adapters.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Ивент о том, что не хватило денег на оплату заказа от billing сервиса.
 */
@Data
public class OrderPaymentEvent {

    /**
     * Идентификатор пользователя.
     */
    private String userId;
    /**
     * Идентификатор заказа.
     */
    private Long orderId;

    @JsonCreator
    public OrderPaymentEvent(@JsonProperty("userId") String userId,
                             @JsonProperty("orderId") Long orderId) {
        this.userId = userId;
        this.orderId = orderId;
    }

}
