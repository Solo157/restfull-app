package com.service.adapter.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.adapter.OrderItemDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Команда на освобождение зарезервированного курьера (компенсация), приходящая от order-service.
 */
@Data
@NoArgsConstructor
public class ReleaseDeliveryCommand {

    /**
     * Уникальный идентификатор saga.
     */
    private UUID sagaId;

    /**
     * Идентификатор заказа.
     */
    private Long orderId;

    /**
     * Идентификатор пользователя, оформившего заказ.
     */
    private String userId;

    /**
     * Ключ идемпотентности для защиты от дублирования обработки команды.
     */
    private String keyIdempotence;

    @JsonCreator
    public ReleaseDeliveryCommand(@JsonProperty("sagaId") UUID sagaId,
                                  @JsonProperty("orderId") Long orderId,
                                  @JsonProperty("userId") String userId,
                                  @JsonProperty("keyIdempotence") String keyIdempotence) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
        this.keyIdempotence = keyIdempotence;
    }

}
