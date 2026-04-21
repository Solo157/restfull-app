package com.service.database;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.*;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Integer amount;

    /**
     * Товары заказа.
     */
    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items;

    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;

    private String deliveryAddress;
    private String contactPhone;
    private String orderDate;

    @PersistenceCreator
    public Order() {
    }

}
