package com.service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.service.service.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final NotificationService notificationService;

    private static final String QUEUE_NAME = "order_status_event";
    private static final String QUEUE_NAME2 = "order_no_money_event";
    private final ObjectMapper objectMapper;
    private ConnectionFactory connectionFactory;

    @Autowired
    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.objectMapper = new ObjectMapper();
        getConnectionFactory();
    }

    private void getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.0.106"); // укажите ваш хост
        factory.setPort(5672); // порт, если не стандартный
        factory.setUsername("guest");
        factory.setPassword("guest");
        connectionFactory = factory;
    }

    @PostConstruct
    void start() {
        System.out.println("StartConsuming...");
        try {
            startConsumingOrderStatus();
            startConsumingNoMoney();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void startConsumingOrderStatus() throws IOException, TimeoutException {
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // Объявляем очередь, чтобы убедиться, что она существует
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        // Создаем потребителя
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageBody = new String(delivery.getBody(), StandardCharsets.UTF_8);
            try {
                OrderStatusEventDTO event = objectMapper.readValue(messageBody, OrderStatusEventDTO.class);
                handleOrderStatusEvent(event);
            } catch (Exception e) {
                // обработка ошибок парсинга
                e.printStackTrace();
            }
        };

        // Начинаем потреблять сообщения
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }

    public void startConsumingNoMoney() throws IOException, TimeoutException {
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // Объявляем очередь, чтобы убедиться, что она существует
        channel.queueDeclare(QUEUE_NAME2, true, false, false, null);

        // Создаем потребителя
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageBody = new String(delivery.getBody(), StandardCharsets.UTF_8);
            try {
                OrderNoMoneyEventDTO event = objectMapper.readValue(messageBody, OrderNoMoneyEventDTO.class);
                handleOrderNoMoneyEvent(event);
            } catch (Exception e) {
                // обработка ошибок парсинга
                e.printStackTrace();
            }
        };

        // Начинаем потреблять сообщения
        channel.basicConsume(QUEUE_NAME2, true, deliverCallback, consumerTag -> { });
    }



    private void handleOrderStatusEvent(OrderStatusEventDTO event) {
        // Обработка полученного события
        System.out.println("Received event for userId: " + event.getUserId() + ", amount: " + event.getAmount());
        // тут ваша логика обработки
        notificationService.handleOrderStatusEvent(event);
    }

    private void handleOrderNoMoneyEvent(OrderNoMoneyEventDTO event) {
        // Обработка полученного события
        System.out.println("Received event for userId: " + event.getUserId() + ", amount: " + event.getOrderAmount());
        // тут ваша логика обработки
        notificationService.handleOrderNoMoneyEvent(event);
    }

}