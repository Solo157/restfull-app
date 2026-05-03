package com.service.saga;

public enum SagaStep {
    PAYMENT,
    INVENTORY,
    DELIVERY,
    COMPLETED,
    COMPENSATING;

    public SagaStep getNextStep() {
        return switch (this) {
            case PAYMENT -> INVENTORY;
            case INVENTORY -> DELIVERY;
            case DELIVERY -> COMPLETED;
            default -> null;
        };
    }

}
