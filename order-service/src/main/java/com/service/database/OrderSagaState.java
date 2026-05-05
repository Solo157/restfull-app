package com.service.database;

import com.service.saga.OrderSagaStatus;
import com.service.saga.OrderSagaStep;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Состояние саги заказа.
 */
@Entity
@Table(name = "order_saga_state")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSagaState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private UUID sagaId;

    private Long orderId;
    private String userId;
    /**
     * Статус саги.
     */
    private OrderSagaStatus orderSagaStatus;
    /**
     * Текущий шаг саги.
     */
    private OrderSagaStep currentStep;
    /**
     * Завершенные шаги саги.
     */
    private LinkedList<OrderSagaStep> completedSteps;
    /**
     * Сообщение об ошибке, в случае, если сага закончилась неуспешно.
     */
    private String errorMessage;

    private Date createdAt;
    private Date updatedAt;

    public OrderSagaState(UUID sagaId, Long orderId, String userId, OrderSagaStatus orderSagaStatus, OrderSagaStep currentStep,
                          LinkedList<OrderSagaStep> completedSteps, String errorMessage, Date createdAt, Date updatedAt) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
        this.orderSagaStatus = orderSagaStatus;
        this.currentStep = currentStep;
        this.completedSteps = completedSteps;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
