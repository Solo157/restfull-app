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
     * Ключ для отправки всем подписчикам что нет денег для оплаты заказа.
     */
    public static final String PAYMENT_NO_MONEY_KEY = "payment.no_money";
    /**
     * Ключ для отправки всем подписчикам что заказ успешно оплачен и деньги списались.
     */
    public static final String PAYMENT_SUCCEEDED_KEY = "payment.succeeded";

    /**
     * Ключ для отправки команды списания средств со счета пользователя по заказу.
     */
    public static final String RESERVE_PAYMENT_COMMAND_KEY = "reserve.payment.command";

    /**
     * Очередь, которая обрабатывает ивент о создании заказа.
     */
    public static final String BILLING_ORDER_CREATED_QUEUE = "billing.order.created";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(ORDER_EVENTS_TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue billingOrderCreatedQueue() {
        return new Queue(BILLING_ORDER_CREATED_QUEUE, true, false, false);
    }

    /**
     * Привязка очереди в биллинг-сервисе на ключ по созданию ордера.
     */
    @Bean
    public Binding bindBillingOrderCreatedQueueToExchange(Queue billingOrderCreatedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(billingOrderCreatedQueue)
                .to(topicExchange)
                .with(ORDER_CREATED_KEY);
    }

}
