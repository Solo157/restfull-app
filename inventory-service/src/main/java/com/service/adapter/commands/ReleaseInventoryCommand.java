package com.service.adapter.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.adapter.OrderItemDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Команда на освобождение зарезервированных товаров (компенсация), приходящая от order-service.
 */
@Data
@NoArgsConstructor
public class ReleaseInventoryCommand {

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
     * Список позиций заказа, которые необходимо вернуть на склад.
     */
    private List<OrderItemDTO> items;

    /**
     * Ключ идемпотентности для защиты от дублирования обработки команды.
     */
    private String keyIdempotence;

    @JsonCreator
    public ReleaseInventoryCommand(@JsonProperty("sagaId") UUID sagaId,
                                   @JsonProperty("orderId") Long orderId,
                                   @JsonProperty("userId") String userId,
                                   @JsonProperty("items") List<OrderItemDTO> items,
                                   @JsonProperty("keyIdempotence") String keyIdempotence) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.keyIdempotence = keyIdempotence;
    }

}
