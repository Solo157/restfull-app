package com.service.saga.command;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
public class ReleaseDeliveryCommand {

    private UUID sagaId;
    private Long orderId;
    private String userId;
    private Integer amount;
    private String keyIdempotence;

    @JsonCreator
    public ReleaseDeliveryCommand(@JsonProperty("sagaId") UUID sagaId,
                                  @JsonProperty("orderId") Long orderId,
                                  @JsonProperty("userId") String userId,
                                  @JsonProperty("amount") Integer amount,
                                  @JsonProperty("keyIdempotence") String keyIdempotence) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.keyIdempotence = keyIdempotence;
   }

}
