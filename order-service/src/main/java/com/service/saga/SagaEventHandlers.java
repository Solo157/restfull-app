package com.service.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.config.RabbitConfig;
import com.service.saga.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Обработчик событий от downstream-сервисов (billing, inventory, delivery)
 * в рамках saga-оркестрации заказа. Де_serialизует входящие сообщения из RabbitMQ
 * и передаёт управление в SagaCoordinator для перехода к следующему шагу
 * или запуска процедуры компенсации.
 */
@Component
@RequiredArgsConstructor
public class SagaEventHandlers {

    private final SagaCoordinator sagaCoordinator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.PAYMENT_RESERVED_COMMAND_QUEUE)
    public void handlePaymentReserved(String messageBody) {
        System.out.println("Received message: " + messageBody);

        PaymentReservedEvent event = deserializeEvent(messageBody, PaymentReservedEvent.class);
        if (event == null) {
            return;
        }

        if (!event.isSuccess()) {
            sagaCoordinator.compensateSaga(event.getSagaId(), event.getMessage());
            return;
        }

        sagaCoordinator.advanceToNextStep(event.getSagaId());
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_RELEASED_COMMAND_QUEUE)
    public void handlePaymentReleased(String messageBody) {
        System.out.println("Received message: " + messageBody);

        PaymentReleasedEvent event = deserializeEvent(messageBody, PaymentReleasedEvent.class);
        if (event == null) {
            return;
        }

        // т.к. это последний коспенсирующий шаг в саге, то должны завершать неудачную сагу.
        sagaCoordinator.completeFailedSaga(event.getSagaId());
    }

    @RabbitListener(queues = RabbitConfig.INVENTORY_RESERVED_QUEUE)
    public void handleInventoryReserved(String messageBody) {
        System.out.println("Received message: " + messageBody);

        InventoryReservedEvent event = deserializeEvent(messageBody, InventoryReservedEvent.class);
        if (event == null) {
            return;
        }

        if (!event.isSuccess()) {
            sagaCoordinator.compensateSaga(event.getSagaId(), event.getMessage());
            return;
        }
        sagaCoordinator.advanceToNextStep(event.getSagaId());
    }

    @RabbitListener(queues = RabbitConfig.INVENTORY_RELEASED_COMMAND_QUEUE)
    public void handleInventoryReleased(String messageBody) {
        System.out.println("Received message: " + messageBody);

        InventoryReleasedEvent event = deserializeEvent(messageBody, InventoryReleasedEvent.class);
        if (event == null) {
            return;
        }

        sagaCoordinator.sendCompensationCommand(event.getSagaId(), OrderSagaStep.PAYMENT);
    }

    @RabbitListener(queues = RabbitConfig.DELIVERY_RESERVED_COMMAND_QUEUE)
    public void handleDeliveryReserved(String messageBody) {
        System.out.println("Received message: " + messageBody);

        DeliveryReservedEvent event = deserializeEvent(messageBody, DeliveryReservedEvent.class);
        if (event == null) {
            return;
        }

        if (!event.isSuccess()) {
            sagaCoordinator.compensateSaga(event.getSagaId(), event.getMessage());
            return;
        }
        sagaCoordinator.advanceToNextStep(event.getSagaId());
    }

    @RabbitListener(queues = RabbitConfig.DELIVERY_RELEASED_COMMAND_QUEUE)
    public void handleDeliveryReleased(String messageBody) {
        System.out.println("Received message: " + messageBody);

        DeliveryReleasedEvent event = deserializeEvent(messageBody, DeliveryReleasedEvent.class);
        if (event == null) {
            return;
        }

        sagaCoordinator.sendCompensationCommand(event.getSagaId(), OrderSagaStep.INVENTORY);
    }

    private <T> T deserializeEvent(String messageBody, Class<T> eventType) {
        try {
            return objectMapper.readValue(messageBody, eventType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
