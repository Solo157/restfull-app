package com.service.saga;

import com.service.adapters.BillingServiceProxy;
import com.service.adapters.DeliveryServiceProxy;
import com.service.adapters.InventoryServiceProxy;
import com.service.service.OrderManagerService;
import com.service.service.SagaManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SagaCoordinator {

    private final SagaManager sagaManager;
    private final BillingServiceProxy billingServiceProxy;
    private final DeliveryServiceProxy deliveryServiceProxy;
    private final InventoryServiceProxy inventoryServiceProxy;
    private final OrderManagerService orderManagerService;

    /**
     * Переход к следующему шагу саги
     */
    @Transactional
    public void advanceToNextStep(UUID sagaId) {
        Optional<OrderSagaState> orderSagaStateOpt = sagaManager.getOptById(sagaId);
        if (orderSagaStateOpt.isEmpty()) {
            return;
        }

        OrderSagaState state = orderSagaStateOpt.get();

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
        sagaManager.save(state);

        // Отправляем команду на следующий шаг
        sendCommandForStep(state);
    }

    @Transactional
    public void completeSuccessfulSaga(OrderSagaState sagaState) {
        sagaState.getCompletedSteps().add(sagaState.getCurrentStep());
        sagaState.setSagaStatus(SagaStatus.COMPLETED);
        sagaState.setCurrentStep(SagaStep.COMPLETED);
        sagaState.setUpdatedAt(new Date());

        sagaManager.save(sagaState);
        orderManagerService.completeOrder(sagaState.getOrderId(), "order completed");
    }

    public void completeFailedSaga(UUID sagaId) {
        Optional<OrderSagaState> orderSagaStateOpt = sagaManager.getOptById(sagaId);
        if (orderSagaStateOpt.isEmpty()) {
            return;
        }

        OrderSagaState state = orderSagaStateOpt.get();
        state.setSagaStatus(SagaStatus.FAILED);
        state.setUpdatedAt(new Date());
        sagaManager.save(state);

        orderManagerService.cancelOrder(state.getOrderId(), state.getErrorMessage());
    }

    /**
     * Компенсация при ошибке.
     */
    @Transactional
    public void compensateSaga(UUID sagaId, String errorMessage) {
        Optional<OrderSagaState> orderSagaStateOpt = sagaManager.getOptById(sagaId);
        if (orderSagaStateOpt.isEmpty()) {
            return;
        }

        OrderSagaState state = orderSagaStateOpt.get();

        state.setSagaStatus(SagaStatus.FAILED);
        state.setCurrentStep(SagaStep.COMPENSATING);
        state.setErrorMessage(errorMessage);
        state.setUpdatedAt(new Date());
        sagaManager.save(state);

        // Проходим по завершенным шагам в обратном порядке
        List<SagaStep> stepsToCompensate = new ArrayList<>(state.getCompletedSteps());
        Collections.reverse(stepsToCompensate);

        if (stepsToCompensate.isEmpty()) {
            completeFailedSaga(sagaId);
            return;
        }

        sendCompensationCommand(orderSagaStateOpt.get(), stepsToCompensate.getFirst());
    }

    private void sendCommandForStep(OrderSagaState state) {
        switch (state.getCurrentStep()) {
            case PAYMENT -> billingServiceProxy.sendReservePaymentCommand(state);
            case INVENTORY -> inventoryServiceProxy.sendReserveInventoryCommand(state);
            case DELIVERY -> deliveryServiceProxy.sendReserveDeliveryCommand(state);
        }
    }

    public void sendCompensationCommand(UUID sagaId, SagaStep step) {
        Optional<OrderSagaState> orderSagaStateOpt = sagaManager.getOptById(sagaId);
        if (orderSagaStateOpt.isEmpty()) {
            return;
        }

        sendCompensationCommand(orderSagaStateOpt.get(), step);
    }

    private void sendCompensationCommand(OrderSagaState state, SagaStep step) {
        switch (step) {
            case PAYMENT -> billingServiceProxy.sendReleasePaymentCommand(state);
            case INVENTORY -> inventoryServiceProxy.sendReleaseInventoryCommand(state);
            case DELIVERY -> deliveryServiceProxy.sendReleaseDeliveryCommand(state);
        }
    }

}
