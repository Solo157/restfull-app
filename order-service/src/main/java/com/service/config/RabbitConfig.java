package com.service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /**
     * Общий топик заказов. Через него происходит адресация в брокере RabbitMQ.
     */
    public static final String ORDER_EVENTS_TOPIC_EXCHANGE = "order.events";

    /**
     * Ключ для отправки в сервис нотификаций о том, что заказ отменен из-за недостатка средств.
     */
    public static final String ORDER_CANCELLED_NO_MONEY_KEY = "order.cancelled.no_money";

    /**
     * Ключ для отправки в сервис нотификаций о том, что заказ успешно завершен.
     */
    public static final String ORDER_COMPLETED_KEY = "order.completed";

    /**
     * Ключ для отправки команды резервирования средств в биллинг-сервис.
     */
    public static final String RESERVE_PAYMENT_COMMAND_KEY = "reserve.payment.command";

    /**
     * Ключ для отправки ивента из биллинг-сервиса о том, что средства успешно зарезервированы.
     */
    public static final String PAYMENT_RESERVED_EVENT_KEY = "payment.reserved.event";

    /**
     * Очередь для обработки команды о том, что средства успешно сняты по заказу.
     */
    public static final String PAYMENT_RESERVED_COMMAND_QUEUE = "payment.reserved.command.queue";

    /**
     * Ключ для отправки команды освобождения средств в биллинг-сервис.
     */
    public static final String RELEASE_PAYMENT_COMMAND_KEY = "release.payment.command";

    /**
     * Ключ для отправки ивента из биллинг-сервиса о том, что средства возвращены.
     */
    public static final String PAYMENT_RELEASED_EVENT_KEY = "payment.released.event";

    /**
     * Очередь для обработки команды о том, что средства не сняты и возвращены по заказу.
     */
    public static final String PAYMENT_RELEASED_COMMAND_QUEUE = "payment.released.command.queue";

    /**
     * Ключ для отправки команды резервирования курьера в доставку.
     */
    public static final String RESERVE_DELIVERY_COMMAND_KEY = "reserve.delivery.command";

    /**
     * Ключ для отправки ивента о том, что курьер успешно назначен на заказ.
     */
    public static final String DELIVERY_RESERVED_EVENT_KEY = "delivery.reserved.event";

    /**
     * Очередь для обработки команды назначения курьера по заказу.
     */
    public static final String DELIVERY_RESERVED_COMMAND_QUEUE = "delivery.reserved.command.queue";

    /**
     * Ключ для отправки команды снятия курьера с заказа.
     */
    public static final String RELEASE_DELIVERY_COMMAND_KEY = "release.delivery.command";

    /**
     * Ключ для отправки ивента о том, что курьер освобожден и снят с заказа.
     */
    public static final String DELIVERY_RELEASED_EVENT_KEY = "delivery.released.event";

    /**
     * Очередь для обработки команды снятия курьера с заказа.
     */
    public static final String DELIVERY_RELEASED_COMMAND_QUEUE = "delivery.released.command.queue";

    /**
     * Ключ для отправки команды резервирования товара в инвентарь.
     */
    public static final String RESERVE_INVENTORY_COMMAND_KEY = "reserve.inventory.command";

    /**
     * Ключ для отправки ивента о том, что товар успешно зарезервирован.
     */
    public static final String INVENTORY_RESERVED_EVENT_KEY = "inventory.reserved.event";

    /**
     * Очередь для обработки команды резервирования товара.
     */
    public static final String INVENTORY_RESERVED_QUEUE = "inventory.reserved.command.queue";

    /**
     * Ключ для отправки команды возврата товара в инвентарь.
     */
    public static final String RELEASE_INVENTORY_COMMAND_KEY = "release.inventory.command";

    /**
     * Ключ для отправки ивента о том, что товар возвращен на склад.
     */
    public static final String INVENTORY_RELEASED_EVENT_KEY = "inventory.released.event";

    /**
     * Очередь для обработки команды возврата товара.
     */
    public static final String INVENTORY_RELEASED_COMMAND_QUEUE = "inventory.released.command.queue";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(ORDER_EVENTS_TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentReservedQueue() {
        return new Queue(PAYMENT_RESERVED_COMMAND_QUEUE, true, false, false);
    }

    @Bean
    public Queue paymentReleasedQueue() {
        return new Queue(PAYMENT_RELEASED_COMMAND_QUEUE, true, false, false);
    }

    @Bean
    public Queue inventoryReservedQueue() {
        return new Queue(INVENTORY_RESERVED_QUEUE, true, false, false);
    }

    @Bean
    public Queue inventoryReleasedQueue() {
        return new Queue(INVENTORY_RELEASED_COMMAND_QUEUE, true, false, false);
    }

    @Bean
    public Queue deliveryReservedQueue() {
        return new Queue(DELIVERY_RESERVED_COMMAND_QUEUE, true, false, false);
    }

    @Bean
    public Queue deliveryReleasedQueue() {
        return new Queue(DELIVERY_RELEASED_COMMAND_QUEUE, true, false, false);
    }

    /**
     * Привязка очереди резервирования оплаты к обмену с ключем события о резервировании платежа.
     */
    @Bean
    public Binding bindPaymentReservedQueueToExchange(Queue paymentReservedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(paymentReservedQueue)
                .to(topicExchange)
                .with(PAYMENT_RESERVED_EVENT_KEY);
    }

    /**
     * Привязка очереди освобождения оплаты к обмену с ключем события о возврате платежа.
     */
    @Bean
    public Binding bindPaymentReleasedQueueToExchange(Queue paymentReleasedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(paymentReleasedQueue)
                .to(topicExchange)
                .with(PAYMENT_RELEASED_EVENT_KEY);
    }

    /**
     * Привязка очереди резервирования инвентаря к обмену с ключем события о резервировании товара.
     */
    @Bean
    public Binding bindInventoryReservedQueueToExchange(Queue inventoryReservedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(inventoryReservedQueue)
                .to(topicExchange)
                .with(INVENTORY_RESERVED_EVENT_KEY);
    }

    /**
     * Привязка очереди возврата инвентаря к обмену с ключем события о возврате товара.
     */
    @Bean
    public Binding bindInventoryReleasedQueueToExchange(Queue inventoryReleasedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(inventoryReleasedQueue)
                .to(topicExchange)
                .with(INVENTORY_RELEASED_EVENT_KEY);
    }

    /**
     * Привязка очереди резервирования доставки к обмену с ключем события о назначении курьера.
     */
    @Bean
    public Binding bindDeliveryReservedQueueToExchange(Queue deliveryReservedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(deliveryReservedQueue)
                .to(topicExchange)
                .with(DELIVERY_RESERVED_EVENT_KEY);
    }

    /**
     * Привязка очереди освобождения доставки к обмену с ключем события о снятии курьера с заказа.
     */
    @Bean
    public Binding bindDeliveryReleasedQueueToExchange(Queue deliveryReleasedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(deliveryReleasedQueue)
                .to(topicExchange)
                .with(DELIVERY_RELEASED_EVENT_KEY);
    }

}
