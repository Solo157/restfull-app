package com.service.saga;

import com.service.adapters.BillingServiceProxy;
import com.service.adapters.DeliveryServiceProxy;
import com.service.adapters.InventoryServiceProxy;
import com.service.database.OrderSagaState;
import com.service.service.OrderManagerService;
import com.service.service.SagaManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Сервис координации саг.
 */
@Service
@RequiredArgsConstructor
public class SagaCoordinator {

    private final SagaManager sagaManager;
    private final BillingServiceProxy billingServiceProxy;
    private final DeliveryServiceProxy deliveryServiceProxy;
    private final InventoryServiceProxy inventoryServiceProxy;
    private final OrderManagerService orderManagerService;

    /**
     * Переход к следующему шагу саги.
     */
    @Transactional
    public void advanceToNextStep(UUID sagaId) {
        Optional<OrderSagaState> orderSagaStateOpt = sagaManager.getOptById(sagaId);
        if (orderSagaStateOpt.isEmpty()) {
            return;
        }

        OrderSagaState state = orderSagaStateOpt.get();

        if (state.getOrderSagaStatus() != OrderSagaStatus.IN_PROGRESS) {
            throw new IllegalStateException("Saga is not in progress");
        }

        OrderSagaStep nextStep = state.getCurrentStep().getNextStep();
        if (nextStep == null || nextStep == OrderSagaStep.COMPLETED) {
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

    /**
     * Завершить сагу успешно.
     */
    @Transactional
    public void completeSuccessfulSaga(OrderSagaState sagaState) {
        sagaState.getCompletedSteps().add(sagaState.getCurrentStep());
        sagaState.setOrderSagaStatus(OrderSagaStatus.COMPLETED);
        sagaState.setCurrentStep(OrderSagaStep.COMPLETED);
        sagaState.setUpdatedAt(new Date());

        sagaManager.save(sagaState);
        orderManagerService.completeOrder(sagaState.getOrderId(), "order completed");
    }

    /**
     * Завершить сагу неудачей.
     */
    public void completeFailedSaga(UUID sagaId) {
        Optional<OrderSagaState> orderSagaStateOpt = sagaManager.getOptById(sagaId);
        if (orderSagaStateOpt.isEmpty()) {
            return;
        }

        OrderSagaState state = orderSagaStateOpt.get();
        state.setOrderSagaStatus(OrderSagaStatus.FAILED);
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

        state.setOrderSagaStatus(OrderSagaStatus.FAILED);
        state.setCurrentStep(OrderSagaStep.COMPENSATING);
        state.setErrorMessage(errorMessage);
        state.setUpdatedAt(new Date());
        sagaManager.save(state);

        // Проходим по завершенным шагам в обратном порядке
        List<OrderSagaStep> stepsToCompensate = new ArrayList<>(state.getCompletedSteps());
        Collections.reverse(stepsToCompensate);

        if (stepsToCompensate.isEmpty()) {
            completeFailedSaga(sagaId);
            return;
        }

        sendCompensationCommand(orderSagaStateOpt.get(), stepsToCompensate.getFirst());
    }

    /**
     * Отправить команду по определенному текущему шагу.
     */
    private void sendCommandForStep(OrderSagaState state) {
        switch (state.getCurrentStep()) {
            case PAYMENT -> billingServiceProxy.sendReservePaymentCommand(state);
            case INVENTORY -> inventoryServiceProxy.sendReserveInventoryCommand(state);
            case DELIVERY -> deliveryServiceProxy.sendReserveDeliveryCommand(state);
        }
    }

    /**
     * Отправить компенсирующую команду.
     */
    public void sendCompensationCommand(UUID sagaId, OrderSagaStep step) {
        Optional<OrderSagaState> orderSagaStateOpt = sagaManager.getOptById(sagaId);
        if (orderSagaStateOpt.isEmpty()) {
            return;
        }

        sendCompensationCommand(orderSagaStateOpt.get(), step);
    }

    private void sendCompensationCommand(OrderSagaState state, OrderSagaStep step) {
        switch (step) {
            case PAYMENT -> billingServiceProxy.sendReleasePaymentCommand(state);
            case INVENTORY -> inventoryServiceProxy.sendReleaseInventoryCommand(state);
            case DELIVERY -> deliveryServiceProxy.sendReleaseDeliveryCommand(state);
        }
    }

}
