
package com.service.saga.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
public class InventoryReservedEvent {

    private UUID sagaId;
    private Long orderId;
    private boolean success;
    private String errorMessage;

    @JsonCreator
    public InventoryReservedEvent(@JsonProperty("sagaId") UUID sagaId,
                                  @JsonProperty("orderId") Long orderId,
                                  @JsonProperty("success") boolean success,
                                  @JsonProperty("errorMessage") String errorMessage) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.success = success;
        this.errorMessage = errorMessage;
    }

}