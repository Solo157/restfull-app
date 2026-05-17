package com.service.adapter.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Событие об успешности возврата зарезервированных средств, отправляемое в order-service.
 */
@Data
@NoArgsConstructor
public class PaymentReleasedEvent {

    /**
     * Уникальный идентификатор saga.
     */
    private UUID sagaId;

    /**
     * Идентификатор заказа.
     */
    private Long orderId;

    /**
     * Флаг успешности операции возврата средств.
     */
    private boolean success;

    /**
     * Сообщение с описанием результата операции.
     */
    private String message;

    @JsonCreator
    public PaymentReleasedEvent(@JsonProperty("sagaId") UUID sagaId,
                                @JsonProperty("orderId") Long orderId,
                                @JsonProperty("success") boolean success,
                                @JsonProperty("message") String message) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.success = success;
        this.message = message;
    }

}
