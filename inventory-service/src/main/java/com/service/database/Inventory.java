package com.service.database;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productName;
    private Integer count;

    @PersistenceCreator
    public Inventory() {
    }

}
