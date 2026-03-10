package com.service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class RabbitAdapterService {

    private final ConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_NAME = "order_no_money_event";
    private static final String QUEUE_NAME2 = "order_status_event";

    public RabbitAdapterService() {
        this.connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.0.106");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        this.objectMapper = new ObjectMapper();
    }

    public void sendOrderNoMoneyEvent(String userId, String orderId, Long orderAmount) {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // Объявляем очередь, если она еще не создана
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            // Создаем DTO и сериализуем в JSON
            String payload = objectMapper.writeValueAsString(new OrderNoMoneyEventDTO(userId, orderId, orderAmount.toString()));

            // Отправляем сообщение
            channel.basicPublish("", QUEUE_NAME, null, payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // обработка ошибок
            e.printStackTrace();
        }
    }

    public void sendOrderPaymentStatusEvent(String userId, Long orderId, OrderStatus orderStatus, PaymentStatus paymentStatus, Long orderAmount) {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // Объявляем очередь, если она еще не создана
            channel.queueDeclare(QUEUE_NAME2, true, false, false, null);

            // Создаем DTO и сериализуем в JSON
            String payload = objectMapper.writeValueAsString(new OrderStatusEventDTO(userId, orderId, orderStatus, paymentStatus, orderAmount));

            // Отправляем сообщение
            channel.basicPublish("", QUEUE_NAME2, null, payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // обработка ошибок
            e.printStackTrace();
        }
    }


}
