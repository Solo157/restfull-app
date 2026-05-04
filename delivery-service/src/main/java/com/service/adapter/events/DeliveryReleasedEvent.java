package com.service.adapter.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Событие об успешности освобождения курьера, отправляемое в order-service.
 */
@Data
@NoArgsConstructor
public class DeliveryReleasedEvent {

    /**
     * Уникальный идентификатор saga-процесса.
     */
    private UUID sagaId;

    /**
     * Идентификатор заказа.
     */
    private Long orderId;

    /**
     * Флаг успешности операции снятия курьера.
     */
    private boolean success;

    /**
     * Сообщение с описанием результата операции.
     */
    private String message;

    @JsonCreator
    public DeliveryReleasedEvent(@JsonProperty("sagaId") UUID sagaId,
                                 @JsonProperty("orderId") Long orderId,
                                 @JsonProperty("success") boolean success,
                                 @JsonProperty("message") String message) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.success = success;
        this.message = message;
    }

}
