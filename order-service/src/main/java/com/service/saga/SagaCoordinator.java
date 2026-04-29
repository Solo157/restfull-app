package com.service.saga;

import com.service.adapters.BillingServiceProxy;
import com.service.adapters.DeliveryServiceProxy;
import com.service.adapters.InventoryServiceProxy;
import com.service.database.SagaStateRepository;
import com.service.service.OrderManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SagaCoordinator {

    private final SagaStateRepository sagaStateRepository;
    private final BillingServiceProxy billingServiceProxy;
    private final DeliveryServiceProxy deliveryServiceProxy;
    private final InventoryServiceProxy inventoryServiceProxy;
    private final OrderManagerService orderManagerService;

    /**
     * Запуск новой саги
     */
    @Transactional
    public OrderSagaState getStartSagaState(UUID sagaId, Long orderId, String userId, Integer amount) {
        OrderSagaState sagaState = new OrderSagaState(
                sagaId,
                orderId,
                userId,
                amount,
                SagaStatus.STARTED,
                SagaStep.PAYMENT,
                new LinkedList<>(),
                new Date(),
                new Date()
        );
        return sagaStateRepository.save(sagaState);
    }

    /**
     * Переход к следующему шагу саги
     */
    @Transactional
    public void advanceToNextStep(UUID sagaId) {
        OrderSagaState state = sagaStateRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));

        if (state.getSagaStatus() != SagaStatus.IN_PROGRESS) {
            throw new IllegalStateException("Saga is not in progress");
        }

        SagaStep nextStep = state.getCurrentStep().getNextStep();
        if (nextStep == null || nextStep == SagaStep.COMPLETED) {
            completeSuccessfulSaga(state);
            return;
        }

        state.getCompletedSteps().add(state.getCurrentStep());
        state.setCurrentStep(nextStep);
        state.setUpdatedAt(new Date());
        sagaStateRepository.save(state);

        // Отправляем команду на следующий шаг
        sendCommandForStep(state, nextStep);
    }

    @Transactional
    public void completeSuccessfulSaga(OrderSagaState sagaState) {
        sagaState.getCompletedSteps().add(sagaState.getCurrentStep());
        sagaState.setCurrentStep(SagaStep.COMPLETED);
        sagaState.setUpdatedAt(new Date());

        sagaStateRepository.save(sagaState);
        orderManagerService.completeOrder(sagaState.getOrderId(), "order completed");
    }

    @Transactional
    public void completeFailedSaga(Long orderId, String errorMessage) {
        orderManagerService.cancelOrder(orderId, errorMessage);
    }

    /**
     * Компенсация при ошибке.
     */
    @Transactional
    public void compensateSaga(UUID sagaId) {
        OrderSagaState state = sagaStateRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));

        state.setSagaStatus(SagaStatus.FAILED);
        state.setCurrentStep(SagaStep.COMPENSATING);
        state.setUpdatedAt(new Date());
        sagaStateRepository.save(state);

        // Проходим по завершенным шагам в обратном порядке
        List<SagaStep> stepsToCompensate = new ArrayList<>(state.getCompletedSteps());
        Collections.reverse(stepsToCompensate);

        sendCompensationCommand(state, stepsToCompensate.getFirst());
    }

    private void sendCommandForStep(OrderSagaState state, SagaStep step) {
        switch (step) {
            case PAYMENT -> billingServiceProxy.sendReservePaymentCommand(state);
            case INVENTORY -> inventoryServiceProxy.sendReserveInventoryCommand(state);
            case DELIVERY -> deliveryServiceProxy.sendReserveDeliveryCommand(state);
        }
    }

    public void sendCompensationCommand(UUID sagaId, SagaStep step) {
        OrderSagaState state = sagaStateRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));

        sendCompensationCommand(state, step);
    }

    private void sendCompensationCommand(OrderSagaState state, SagaStep step) {
        switch (step) {
            case PAYMENT -> billingServiceProxy.sendReleasePaymentCommand(state);
            case INVENTORY -> inventoryServiceProxy.sendReleaseInventoryCommand(state);
            case DELIVERY -> deliveryServiceProxy.sendReleaseDeliveryCommand(state);
        }
    }

}
