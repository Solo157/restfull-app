package com.service.database;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Long amount;

    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;

    @PersistenceCreator
    public Order() {}

}
