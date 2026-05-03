package com.service.adapters;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.adapters.events.OrderNotificationEvent;
import com.service.database.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.service.config.RabbitConfig.ORDER_COMPLETED_KEY;
import static com.service.config.RabbitConfig.ORDER_EVENTS_TOPIC_EXCHANGE;

@Service
@RequiredArgsConstructor
public class NotificationServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

//    /**
//     * Отправить ивент в сервис нотификаций о том, что не хватило денег на оплату заказа.
//     */
//    public void sendOrderCancelledNoMoneyEvent(Order order) {
//        try {
//            String payload = objectMapper.writeValueAsString(
//                    new OrderNotificationEvent(order.getUserId(),
//                            order.getId(),
//                            order.getItems(),
//                            order.getDeliveryAddress(),
//                            order.getContactPhone(),
//                            order.getOrderDate(),
//                            order.getAmount()
//                    )
//            );
//
//            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, ORDER_CANCELLED_NO_MONEY_KEY, payload);
//            System.out.println("Message sent");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Отправить ивент в сервис нотификаций о том, что заказ оплачен и завершен.
     */
    public void sendOrderCompletedEvent(Order order, String message) {
        try {
            String payload = objectMapper.writeValueAsString(
                    new OrderNotificationEvent(order.getUserId(),
                            order.getId(),
                            order.getItems(),
                            order.getDeliveryAddress(),
                            order.getContactPhone(),
                            order.getOrderDate(),
                            message,
                            order.getAmount()
                    )
            );

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, ORDER_COMPLETED_KEY, payload);
            System.out.println("Message sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
