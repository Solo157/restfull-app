package com.service.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.service.database.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class RabbitAdapterService {

    private final ConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_NAME = "order_status_event";

    public RabbitAdapterService() {
        this.connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.0.106");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        this.objectMapper = new ObjectMapper();
    }

    public boolean sendOrderStatusEvent(Order order) {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // Объявляем очередь, если она еще не создана
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            // Создаем DTO и сериализуем в JSON
            String payload = objectMapper.writeValueAsString(new OrderStatusEventDTO(order.getUserId(), order.getId(), order.getOrderStatus(), order.getPaymentStatus(), order.getAmount()));

            // Отправляем сообщение
            channel.basicPublish("", QUEUE_NAME, null, payload.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            // обработка ошибок
            e.printStackTrace();
            return false;
        }
    }


}
