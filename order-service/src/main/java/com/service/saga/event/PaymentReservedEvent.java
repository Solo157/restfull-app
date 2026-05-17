package com.service.saga.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Событие от billing-service о результате резервирования средств пользователя.
 */
@Data
@NoArgsConstructor
public class PaymentReservedEvent {

    /**
     * Уникальный идентификатор saga.
     */
    private UUID sagaId;

    /**
     * Идентификатор заказа.
     */
    private Long orderId;

    /**
     * Флаг успешности операции резервирования средств.
     */
    private boolean success;

    /**
     * Сообщение с описанием результата операции.
     */
    private String message;

    @JsonCreator
    public PaymentReservedEvent(@JsonProperty("sagaId") UUID sagaId,
                                @JsonProperty("orderId") Long orderId,
                                @JsonProperty("success") boolean success,
                                @JsonProperty("message") String message) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.success = success;
        this.message = message;
    }

}
