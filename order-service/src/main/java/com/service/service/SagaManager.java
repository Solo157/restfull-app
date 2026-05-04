package com.service.service;

import com.service.database.SagaStateRepository;
import com.service.saga.OrderSagaState;
import com.service.saga.SagaStatus;
import com.service.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SagaManager {

    private final SagaStateRepository sagaStateRepository;

    public Optional<OrderSagaState> getOptById(UUID id) {
        return sagaStateRepository.findBySagaId(id);
    }

    @Transactional
    public void save(OrderSagaState state) {
        sagaStateRepository.save(state);
    }

    /**
     * Запуск новой саги
     */
    @Transactional
    public OrderSagaState getStartSagaState(UUID sagaId, Long orderId, String userId, Integer amount) {
        OrderSagaState sagaState = new OrderSagaState(
                sagaId,
                orderId,
                userId,
                SagaStatus.STARTED,
                SagaStep.PAYMENT,
                new LinkedList<>(),
                "",
                new Date(),
                new Date()
        );
        return sagaStateRepository.save(sagaState);
    }

}
