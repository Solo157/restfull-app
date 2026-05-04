package com.service.adapter.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.adapter.OrderItemDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Команда на резервирование товаров на складе, приходящая от order-service в рамках saga.
 */
@Data
@NoArgsConstructor
public class ReserveInventoryCommand {

    /**
     * Уникальный идентификатор saga-процесса.
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
     * Список позиций заказа (товары, их цены и количества).
     */
    private List<OrderItemDTO> items;

    /**
     * Ключ идемпотентности для защиты от дублирования обработки команды.
     */
    private String keyIdempotence;

    @JsonCreator
    public ReserveInventoryCommand(@JsonProperty("sagaId") UUID sagaId,
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
