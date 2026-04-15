package com.service.database;

public enum OrderStatus {
    /**
     * Новый заказ.
     */
    NEW,
    /**
     * Заказ в работе, в готовке, в обработке.
     */
    IN_PROCESS,
    /**
     * Заказ завершен.
     */
    COMPLETED,
    /**
     * Заказ отменен.
     */
    CANCELLED

}
