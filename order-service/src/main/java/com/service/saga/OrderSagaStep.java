package com.service.saga;

public enum OrderSagaStep {
    /**
     * Шаг оплаты заказа.
     */
    PAYMENT,
    /**
     * Шаг резервирования товара на складе.
     */
    INVENTORY,
    /**
     * Шаг резервирования курьера.
     */
    DELIVERY,
    /**
     * Шаг в котором ничего делать не нужно. Сага успешно завершена.
     */
    COMPLETED,
    /**
     * Сага в процессе компенсации.
     */
    COMPENSATING;

    public OrderSagaStep getNextStep() {
        return switch (this) {
            case PAYMENT -> INVENTORY;
            case INVENTORY -> DELIVERY;
            case DELIVERY -> COMPLETED;
            default -> null;
        };
    }

}
