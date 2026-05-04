package com.service.saga.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Событие от delivery-service о результате резервирования курьера для доставки.
 */
@Data
@NoArgsConstructor
public class DeliveryReservedEvent {

    /**
     * Уникальный идентификатор saga-процесса.
     */
    private UUID sagaId;

    /**
     * Идентификатор заказа.
     */
    private Long orderId;

    /**
     * Флаг успешности операции резервирования курьера.
     */
    private boolean success;

    /**
     * Сообщение с описанием результата операции.
     */
    private String message;

    @JsonCreator
    public DeliveryReservedEvent(@JsonProperty("sagaId") UUID sagaId,
                                 @JsonProperty("orderId") Long orderId,
                                 @JsonProperty("success") boolean success,
                                 @JsonProperty("message") String message) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.success = success;
        this.message = message;
    }

}
