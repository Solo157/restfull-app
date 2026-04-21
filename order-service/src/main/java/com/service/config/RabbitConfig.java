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
     * Ключ для приема ивентов о том, что заказ был создан.
     */
    public static final String ORDER_CREATED_KEY = "order.created";
    /**
     * Ключ для приема ивентов о том, что заказ отменен по причине нехватки денег.
     */
    public static final String ORDER_CANCELLED_NO_MONEY_KEY = "order.cancelled.no_money";
    /**
     * Ключ для приема ивентов о том, что заказ успешно оплачен и был завершен.
     */
    public static final String ORDER_COMPLETED_KEY = "order.completed";

    /**
     * Ключ для отправки всем подписчикам что нет денег для оплаты заказа.
     */
    public static final String PAYMENT_NO_MONEY_KEY = "payment.no_money";
    /**
     * Очередь для приема ивентов по заказам у которых не хватило денег на оплату.
     */
    public static final String ORDER_PAYMENT_NO_MONEY_QUEUE = "order.payment.no_money";

    /**
     * Ключ для отправки всем подписчикам что заказ успешно оплачен и деньги списались.
     */
    public static final String PAYMENT_SUCCEEDED_KEY = "payment.succeeded";
    /**
     * Очередь для приема ивентов по заказам у которых было успешной списание по заказу.
     */
    public static final String ORDER_PAYMENT_SUCCEEDED_QUEUE = "order.payment.succeeded";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(ORDER_EVENTS_TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderPaymentNoMoneyQueue() {
        return new Queue(ORDER_PAYMENT_NO_MONEY_QUEUE, true, false, false);
    }

    @Bean
    public Queue orderPaymentSucceededQueue() {
        return new Queue(ORDER_PAYMENT_SUCCEEDED_QUEUE, true, false, false);
    }

    /**
     * Привязка очереди в ордер-сервисе на ключ неуспешной оплаты по причине отсутствия денег.
     */
    @Bean
    public Binding bindOrderPaymentNoMoneyQueueToExchange(Queue orderPaymentNoMoneyQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(orderPaymentNoMoneyQueue)
                .to(topicExchange)
                .with(PAYMENT_NO_MONEY_KEY);
    }

    /**
     * Привязка очереди в ордер-сервисе на ключ успешной оплаты.
     */
    @Bean
    public Binding bindOrderPaymentSucceededQueueToExchange(Queue orderPaymentSucceededQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(orderPaymentSucceededQueue)
                .to(topicExchange)
                .with(PAYMENT_SUCCEEDED_KEY);
    }

}
