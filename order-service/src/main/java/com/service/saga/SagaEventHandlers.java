package com.service.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.config.RabbitConfig;
import com.service.saga.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventHandlers {

    private final SagaCoordinator sagaCoordinator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.PAYMENT_RESERVED_COMMAND_QUEUE)
    public void handlePaymentReserved(String messageBody) {
        PaymentReservedEvent event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, PaymentReservedEvent.class);
            System.out.println("Received event for sagaId: " + event.getSagaId());
        } catch (Exception e) {
            e.printStackTrace();
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
        PaymentReleasedEvent event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, PaymentReleasedEvent.class);
            System.out.println("Received event for sagaId: " + event.getSagaId());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        sagaCoordinator.completeFailedSaga(event.getSagaId());
    }

    @RabbitListener(queues = RabbitConfig.INVENTORY_RESERVED_QUEUE)
    public void handleInventoryReserved(String messageBody) {
        InventoryReservedEvent event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, InventoryReservedEvent.class);
            System.out.println("Received event for sagaId: " + event.getSagaId());
        } catch (Exception e) {
            e.printStackTrace();
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
        InventoryReleasedEvent event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, InventoryReleasedEvent.class);
            System.out.println("Received event for sagaId: " + event.getSagaId());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        sagaCoordinator.sendCompensationCommand(event.getSagaId(), SagaStep.PAYMENT);
    }

    @RabbitListener(queues = RabbitConfig.DELIVERY_RESERVED_COMMAND_QUEUE)
    public void handleDeliveryReserved(String messageBody) {
        DeliveryReservedEvent event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, DeliveryReservedEvent.class);
            System.out.println("Received event for sagaId: " + event.getSagaId());
        } catch (Exception e) {
            e.printStackTrace();
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
        DeliveryReleasedEvent event;
        try {
            System.out.println("Received message: " + messageBody);
            event = objectMapper.readValue(messageBody, DeliveryReleasedEvent.class);
            System.out.println("Received event for sagaId: " + event.getSagaId());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        sagaCoordinator.sendCompensationCommand(event.getSagaId(), SagaStep.INVENTORY);
    }

}
