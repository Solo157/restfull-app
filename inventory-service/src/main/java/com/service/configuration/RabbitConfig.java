package com.service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /**
     * Общий топик заказов. Через него происходит адресация в брокере RabbitMQ.
     */
    public static final String ORDER_EVENTS_TOPIC_EXCHANGE = "order.events";

    public static final String RESERVE_INVENTORY_COMMAND_KEY = "reserve.inventory.command"; // ключ для привязки очереди резервирования товара
    public static final String INVENTORY_RESERVED_EVENT_KEY = "inventory.reserved.event"; // ключ для отправки ивента, что товар зарезервирован
    public static final String INVENTORY_RESERVE_COMMAND_QUEUE = "inventory.reserve.command.queue"; // очередь по обработке резервирования товара

    public static final String RELEASE_INVENTORY_COMMAND_KEY = "release.inventory.command"; // ключ для привязки очереди возврата товара
    public static final String INVENTORY_RELEASED_EVENT_KEY = "inventory.released.event"; // ключ для отправки ивента, что товар возвращен
    public static final String INVENTORY_RELEASE_COMMAND_QUEUE = "inventory.release.command.queue"; // очередь по обработке возврата товара

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(ORDER_EVENTS_TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue inventoryReserveCommandQueue() {
        return new Queue(INVENTORY_RESERVE_COMMAND_QUEUE, true, false, false);
    }

    @Bean
    public Queue inventoryReleaseCommandQueue() {
        return new Queue(INVENTORY_RELEASE_COMMAND_QUEUE, true, false, false);
    }

    /**
     * Привязка очереди в биллинг-сервисе на ключ по созданию ордера.
     */
    @Bean
    public Binding bindInventoryReserveCommandQueueToExchange(Queue inventoryReserveCommandQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(inventoryReserveCommandQueue)
                .to(topicExchange)
                .with(RESERVE_INVENTORY_COMMAND_KEY);
    }

    /**
     * Привязка очереди в биллинг-сервисе на ключ по созданию ордера.
     */
    @Bean
    public Binding bindInventoryReleaseCommandQueuerToExchange(Queue inventoryReleaseCommandQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(inventoryReleaseCommandQueue)
                .to(topicExchange)
                .with(RELEASE_INVENTORY_COMMAND_KEY);
    }

}
