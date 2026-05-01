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

    public static final String RESERVE_PAYMENT_COMMAND_KEY = "reserve.payment.command"; // ключ для привязки очереди по обработке снятия средств
    public static final String PAYMENT_RESERVED_EVENT_KEY = "payment.reserved.event"; // ключ для отправки ивента, что средства списаны
    public static final String BILLING_RESERVE_PAYMENT_COMMAND_QUEUE = "billing.reserve.payment.command.queue"; // очередь по обработке снятия средств

    public static final String RELEASE_PAYMENT_COMMAND_KEY = "release.payment.command"; // ключ для привязки очереди по обработке начисления средств
    public static final String PAYMENT_RELEASED_EVENT_KEY = "payment.released.event"; // ключ для отправки ивента, что средства начислены
    public static final String BILLING_RELEASE_PAYMENT_COMMAND_QUEUE = "billing.release.payment.command.queue"; // очередь по обработке начисления средств

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(ORDER_EVENTS_TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue billingReservePaymentCommandQueue() {
        return new Queue(BILLING_RESERVE_PAYMENT_COMMAND_QUEUE, true, false, false);
    }

    @Bean
    public Queue billingReleasePaymentCommandQueue() {
        return new Queue(BILLING_RELEASE_PAYMENT_COMMAND_QUEUE, true, false, false);
    }

    /**
     * Привязка очереди в биллинг-сервисе на ключ по созданию ордера.
     */
    @Bean
    public Binding bindBillingReservePaymentCommandQueueToExchange(Queue billingReservePaymentCommandQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(billingReservePaymentCommandQueue)
                .to(topicExchange)
                .with(RESERVE_PAYMENT_COMMAND_KEY);
    }

    /**
     * Привязка очереди в биллинг-сервисе на ключ по созданию ордера.
     */
    @Bean
    public Binding bindBillingReleasePaymentCommandQueuerToExchange(Queue billingReleasePaymentCommandQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(billingReleasePaymentCommandQueue)
                .to(topicExchange)
                .with(RELEASE_PAYMENT_COMMAND_KEY);
    }

}
