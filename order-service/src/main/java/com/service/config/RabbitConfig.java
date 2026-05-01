package com.service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ORDER_EVENTS_TOPIC_EXCHANGE = "order.events";

    // для отправки в сервис нотификаций
    public static final String ORDER_CANCELLED_NO_MONEY_KEY = "order.cancelled.no_money";
    public static final String ORDER_COMPLETED_KEY = "order.completed";

    public static final String RESERVE_PAYMENT_COMMAND_KEY = "reserve.payment.command"; // для отправки команды в биллинг
    public static final String PAYMENT_RESERVED_EVENT_KEY = "payment.reserved.event"; // используется биллингом для отправки
    public static final String PAYMENT_RESERVED_COMMAND_QUEUE = "payment.reserved.command.queue"; // очередь что успешно сняли средства по заказу

    public static final String RELEASE_PAYMENT_COMMAND_KEY = "release.payment.command"; // для отправки команды в биллинг
    public static final String PAYMENT_RELEASED_EVENT_KEY = "payment.released.event"; // используется биллингом для отправки
    public static final String PAYMENT_RELEASED_COMMAND_QUEUE = "payment.released.command.queue"; // очередь что не сняли средства по заказу

    public static final String RESERVE_DELIVERY_COMMAND_KEY = "reserve.delivery.command";
    public static final String DELIVERY_RESERVED_EVENT_KEY = "delivery.reserved.event";
    public static final String DELIVERY_RESERVED_COMMAND_QUEUE = "delivery.reserved.command.queue";

    public static final String RELEASE_DELIVERY_COMMAND_KEY = "release.delivery.command";
    public static final String DELIVERY_RELEASED_EVENT_KEY = "delivery.released.event";
    public static final String DELIVERY_RELEASED_COMMAND_QUEUE = "delivery.released.command.queue";

    public static final String RESERVE_INVENTORY_COMMAND_KEY = "reserve.inventory.command";
    public static final String INVENTORY_RESERVED_EVENT_KEY = "inventory.reserved.event";
    public static final String INVENTORY_RESERVED_QUEUE = "inventory.reserved.command.queue";

    public static final String RELEASE_INVENTORY_COMMAND_KEY = "release.inventory.command";
    public static final String INVENTORY_RELEASED_EVENT_KEY = "inventory.released.event";
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

    @Bean
    public Binding bindPaymentReservedQueueToExchange(Queue paymentReservedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(paymentReservedQueue)
                .to(topicExchange)
                .with(PAYMENT_RESERVED_EVENT_KEY);
    }

    @Bean
    public Binding bindPaymentReleasedQueueToExchange(Queue paymentReleasedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(paymentReleasedQueue)
                .to(topicExchange)
                .with(PAYMENT_RELEASED_EVENT_KEY);
    }

    @Bean
    public Binding bindInventoryReservedQueueToExchange(Queue inventoryReservedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(inventoryReservedQueue)
                .to(topicExchange)
                .with(INVENTORY_RESERVED_EVENT_KEY);
    }

    @Bean
    public Binding bindInventoryReleasedQueueToExchange(Queue inventoryReleasedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(inventoryReleasedQueue)
                .to(topicExchange)
                .with(INVENTORY_RELEASED_EVENT_KEY);
    }

    @Bean
    public Binding bindDeliveryReservedQueueToExchange(Queue deliveryReservedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(deliveryReservedQueue)
                .to(topicExchange)
                .with(DELIVERY_RESERVED_EVENT_KEY);
    }

    @Bean
    public Binding bindDeliveryReleasedQueueToExchange(Queue deliveryReleasedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(deliveryReleasedQueue)
                .to(topicExchange)
                .with(DELIVERY_RELEASED_EVENT_KEY);
    }

}
