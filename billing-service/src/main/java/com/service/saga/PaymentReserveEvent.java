package com.service.saga;

import lombok.Data;

import java.util.*;

@Data
public class PaymentReserveEvent {

    private String keyIdempotence;
    private UUID sagaId;
    private Long orderId;
    private String userId;
    private Integer amount;

}
