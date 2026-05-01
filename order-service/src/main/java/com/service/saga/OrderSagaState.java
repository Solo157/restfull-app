package com.service.saga;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private UUID sagaId;

    private Long orderId;
    private String userId;
//    private Integer amount;
    private SagaStatus sagaStatus;
    private SagaStep currentStep;
    private LinkedList<SagaStep> completedSteps;
    private String errorMessage;
    private Date createdAt;
    private Date updatedAt;

}
