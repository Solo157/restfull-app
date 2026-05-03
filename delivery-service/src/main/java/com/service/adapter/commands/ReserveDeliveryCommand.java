package com.service.adapter.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.adapter.OrderItemDTO;
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
                                  @JsonProperty("items") List<OrderItemDTO> items,
                                  @JsonProperty("address") String address,
                                  @JsonProperty("keyIdempotence") String keyIdempotence) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.address = address;
        this.keyIdempotence = keyIdempotence;
    }

}