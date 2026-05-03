package com.service.saga.command;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.api.dto.OrderItemDTO;
import com.service.database.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
public class ReserveDeliveryCommand {

    private UUID sagaId;
    private Long orderId;
    private String userId;
    private List<OrderItemDTO> items;
    private String address;
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