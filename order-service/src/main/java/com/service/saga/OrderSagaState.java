package com.service.saga;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

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
    private SagaStatus sagaStatus;
    private SagaStep currentStep;
    private LinkedList<SagaStep> completedSteps;
    private String errorMessage;
    private Date createdAt;
    private Date updatedAt;

    public OrderSagaState(UUID sagaId, Long orderId, String userId, SagaStatus sagaStatus, SagaStep currentStep,
                          LinkedList<SagaStep> completedSteps, String errorMessage, Date createdAt, Date updatedAt) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.userId = userId;
        this.sagaStatus = sagaStatus;
        this.currentStep = currentStep;
        this.completedSteps = completedSteps;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
