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

    /**
     * Ключ для привязки очереди назначения курьера по заказу.
     */
    public static final String RESERVE_DELIVERY_COMMAND_KEY = "reserve.delivery.command";

    /**
     * Ключ для отправки ивента о том, что курьер успешно назначен на заказ.
     */
    public static final String DELIVERY_RESERVED_EVENT_KEY = "delivery.reserved.event";

    /**
     * Очередь для обработки команды назначения курьера.
     */
    public static final String DELIVERY_RESERVE_COMMAND_QUEUE = "delivery.reserve.command.queue";

    /**
     * Ключ для привязки очереди снятия курьера с заказа.
     */
    public static final String RELEASE_DELIVERY_COMMAND_KEY = "release.delivery.command";

    /**
     * Ключ для отправки ивента о том, что курьер освобожден и снят с заказа.
     */
    public static final String DELIVERY_RELEASED_EVENT_KEY = "delivery.released.event";

    /**
     * Очередь для обработки команды снятия курьера с заказа.
     */
    public static final String DELIVERY_RELEASE_COMMAND_QUEUE = "delivery.release.command.queue";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(ORDER_EVENTS_TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue deliveryReserveCommandQueue() {
        return new Queue(DELIVERY_RESERVE_COMMAND_QUEUE, true, false, false);
    }

    @Bean
    public Queue deliveryReleaseCommandQueue() {
        return new Queue(DELIVERY_RELEASE_COMMAND_QUEUE, true, false, false);
    }

    /**
     * Привязка очереди в доставку-сервисе на ключ резервирования курьера для назначения на заказ.
     */
    @Bean
    public Binding bindDeliveryReserveCommandQueueToExchange(Queue deliveryReserveCommandQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(deliveryReserveCommandQueue)
                .to(topicExchange)
                .with(RESERVE_DELIVERY_COMMAND_KEY);
    }

    /**
     * Привязка очереди в доставку-сервисе на ключ освобождения курьера для снятия с заказа.
     */
    @Bean
    public Binding bindDeliveryReleaseCommandQueuerToExchange(Queue deliveryReleaseCommandQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(deliveryReleaseCommandQueue)
                .to(topicExchange)
                .with(RELEASE_DELIVERY_COMMAND_KEY);
    }

}
