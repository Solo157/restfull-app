package com.service.saga;

import com.service.config.RabbitConfig;
import com.service.saga.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventHandlers {

    private final SagaCoordinator sagaCoordinator;

    @RabbitListener(queues = RabbitConfig.PAYMENT_RESERVED_COMMAND_QUEUE)
    public void handlePaymentReserved(PaymentReservedEvent event) {
        if (!event.isSuccess()) {
            sagaCoordinator.compensateSaga(event.getSagaId());
            return;
        }

        sagaCoordinator.advanceToNextStep(event.getSagaId());
    }

    @RabbitListener(queues = RabbitConfig.COMPENSATION_PAYMENT_RELEASED_COMMAND_QUEUE)
    public void handlePaymentReserved(PaymentReleasedEvent event) {
        sagaCoordinator.completeFailedSaga(event.getOrderId(), event.getErrorMessage());
    }

    @RabbitListener(queues = RabbitConfig.INVENTORY_RESERVED_QUEUE)
    public void handleInventoryReserved(InventoryReservedEvent event) {
        if (!event.isSuccess()) {
            sagaCoordinator.compensateSaga(event.getSagaId());
            return;
        }
        sagaCoordinator.advanceToNextStep(event.getSagaId());
    }

    @RabbitListener(queues = RabbitConfig.COMPENSATION_INVENTORY_RELEASED_COMMAND_QUEUE)
    public void handleInventoryReserved(InventoryReleasedEvent event) {
        sagaCoordinator.sendCompensationCommand(event.getSagaId(), SagaStep.PAYMENT);
    }

    @RabbitListener(queues = RabbitConfig.DELIVERY_RESERVED_COMMAND_QUEUE)
    public void handleDeliveryReserved(DeliveryReservedEvent event) {
        if (!event.isSuccess()) {
            sagaCoordinator.compensateSaga(event.getSagaId());
            return;
        }
        sagaCoordinator.advanceToNextStep(event.getSagaId());
    }

    @RabbitListener(queues = RabbitConfig.COMPENSATION_DELIVERY_RELEASED_COMMAND_QUEUE)
    public void handleDeliveryReserved(DeliveryReleasedEvent event) {
        sagaCoordinator.sendCompensationCommand(event.getSagaId(), SagaStep.INVENTORY);
    }

}
