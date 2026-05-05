package com.service.service;

import com.service.database.OrderSagaState;
import com.service.database.SagaStateRepository;
import com.service.saga.OrderSagaStatus;
import com.service.saga.OrderSagaStep;
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
                OrderSagaStatus.STARTED,
                OrderSagaStep.PAYMENT,
                new LinkedList<>(),
                "",
                new Date(),
                new Date()
        );
        return sagaStateRepository.save(sagaState);
    }

}
