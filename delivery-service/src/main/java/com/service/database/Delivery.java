package com.service.database;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.*;

@Entity
@Table(name = "delivery")
@Data
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courierId;
    @Column(nullable = false, unique = true)
    private Long orderId;

    /**
     * Товары заказа.
     */
    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items;
    private String address;

    @PersistenceCreator
    public Delivery() {
    }

}
