package com.service.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.saga.KeyIdempotence;
import com.service.saga.OrderSagaState;
import com.service.saga.command.ReleasePaymentCommand;
import com.service.saga.command.ReservePaymentCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.service.config.RabbitConfig.COMPENSATION_RELEASE_PAYMENT_COMMAND_KEY;
import static com.service.config.RabbitConfig.RESERVE_PAYMENT_COMMAND_KEY;
import static com.service.config.RabbitConfig.ORDER_EVENTS_TOPIC_EXCHANGE;

@Component
@RequiredArgsConstructor
public class BillingServiceProxy {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Отправить команду на списание суммы по заказа с аккаунта пользователя.
     */
    public void sendReservePaymentCommand(OrderSagaState sagaState) {
        ReservePaymentCommand command = new ReservePaymentCommand();
        command.setSagaId(sagaState.getSagaId());
        command.setOrderId(sagaState.getOrderId());
        command.setUserId(sagaState.getUserId());
        command.setAmount(sagaState.getAmount());
        command.setKeyIdempotence(KeyIdempotence.RESERVE.name());

        try {
            String payload = objectMapper.writeValueAsString(command);

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, RESERVE_PAYMENT_COMMAND_KEY, payload);
            System.out.println("Message sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправить команду на возврат суммы по заказу на счет аккаунта пользователя.
     */
    public void sendReleasePaymentCommand(OrderSagaState sagaState) {
        ReleasePaymentCommand command = new ReleasePaymentCommand();
        command.setSagaId(sagaState.getSagaId());
        command.setOrderId(sagaState.getOrderId());
        command.setUserId(sagaState.getUserId());
        command.setAmount(sagaState.getAmount());
        command.setKeyIdempotence(KeyIdempotence.RELEASE.name());

        try {
            String payload = objectMapper.writeValueAsString(command);

            rabbitTemplate.convertAndSend(ORDER_EVENTS_TOPIC_EXCHANGE, COMPENSATION_RELEASE_PAYMENT_COMMAND_KEY, payload);
            System.out.println("Message sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
