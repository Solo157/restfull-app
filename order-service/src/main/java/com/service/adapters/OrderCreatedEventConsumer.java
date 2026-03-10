package com.service.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.service.service.OrderManagerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private final OrderManagerService orderManagerService;

    private static final String QUEUE_NAME = "order_status_event";
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderCreatedEventConsumer(OrderManagerService orderManagerService) {
        this.orderManagerService = orderManagerService;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    void start() {
        System.out.println("StartConsuming...");
        try {
            startConsuming();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void startConsuming() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.0.106"); // укажите ваш хост
        factory.setPort(5672); // порт, если не стандартный
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Объявляем очередь, чтобы убедиться, что она существует
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        // Создаем потребителя
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageBody = new String(delivery.getBody(), StandardCharsets.UTF_8);
            try {
                OrderStatusEventDTO event = objectMapper.readValue(messageBody, OrderStatusEventDTO.class);
                handleEvent(event);
            } catch (Exception e) {
                // обработка ошибок парсинга
                e.printStackTrace();
            }
        };

        // Начинаем потреблять сообщения
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }

    private void handleEvent(OrderStatusEventDTO event) {
        // Обработка полученного события
        System.out.println("Received event for userId: " + event.getUserId() + ", amount: " + event.getAmount());
        // тут ваша логика обработки
        orderManagerService.handleOrderStatusEvent(event);
    }

}