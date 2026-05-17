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
     * Ключ для приема ивентов о том, что заказ успешно оплачен и был завершен.
     */
    public static final String ORDER_COMPLETED_KEY = "order.completed";

    /**
     * Очередь для обработки ивентов о том, что заказ был успешно завершен.
     */
    public static final String NOTIFICATION_ORDER_COMPLETED_QUEUE = "notification.order.completed";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(ORDER_EVENTS_TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationOrderCompletedQueue() {
        return new Queue(NOTIFICATION_ORDER_COMPLETED_QUEUE, true, false, false);
    }

    /**
     * Привязка очереди в нотификационном сервисе на ключ завершения заказа.
     */
    @Bean
    public Binding bindNotificationOrderCompletedQueueToExchange(Queue notificationOrderCompletedQueue,
                                                                 TopicExchange topicExchange) {
        return BindingBuilder.bind(notificationOrderCompletedQueue)
                .to(topicExchange)
                .with(ORDER_COMPLETED_KEY);
    }

}
