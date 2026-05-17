package com.service.saga.command;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.api.dto.OrderItemDTO;
import com.service.database.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Команда на резервирование курьера для доставки заказа.
 */
@Data
@NoArgsConstructor
public class ReserveDeliveryCommand {

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
     * Список позиций заказа (товары, их цены и количества).
     */
    private List<OrderItemDTO> items;

    /**
     * Адрес доставки заказа.
     */
    private String address;

    /**
     * Ключ идемпотентности для защиты от дублирования обработки команды.
     */
    private String keyIdempotence;

    @JsonCreator
    public ReserveDeliveryCommand(@JsonProperty("sagaId") UUID sagaId,
                                  @JsonProperty("orderId") Long orderId,
                                  @JsonProperty("userId") String userId,
                                  @JsonProperty("items") List<OrderItem> items,
                                  @JsonProperty("address") String address,
                                  @JsonProperty("keyIdempotence") String keyIdempotence) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
        this.address = address;
        this.keyIdempotence = keyIdempotence;

        this.items = items.stream()
                .map(item -> new OrderItemDTO(item.getProductName(), item.getPrice(), item.getCount()))
                .toList();
    }

}
