package com.service.database;

public enum PaymentStatus {
    /**
     * Заказ не оплачен.
     */
    NOT_PAID,
    /**
     * Заказ в ожидании оплаты.
     */
    PENDING,
    /**
     * Заказ успешно оплачен.
     */
    PAID,
    /**
     * Не хватило денег на оплату заказа.
     */
    NO_MONEY

}
