package com.service.saga;

public enum OrderSagaStatus {
    /**
     * Сага готова к обработке.
     */
    STARTED,
    /**
     * Сага в процессе обработки, выполняется.
     */
    IN_PROGRESS,
    /**
     * Сага завершилась неудачно.
     */
    FAILED,
    /**
     * Сага завершилась успешно.
     */
    COMPLETED
}
