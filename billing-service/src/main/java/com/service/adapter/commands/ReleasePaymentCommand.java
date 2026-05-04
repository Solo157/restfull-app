package com.service.adapter.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Команда на возврат зарезервированных средств (компенсация), приходящая от order-service.
 */
@Data
@NoArgsConstructor
public class ReleasePaymentCommand {

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
     * Сумма к возврату на аккаунт пользователя.
     */
    private Integer amount;

    /**
     * Ключ идемпотентности для защиты от дублирования обработки команды.
     */
    private String keyIdempotence;

    @JsonCreator
    public ReleasePaymentCommand(@JsonProperty("sagaId") UUID sagaId,
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
