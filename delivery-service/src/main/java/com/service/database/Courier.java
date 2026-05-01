package com.service.database;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

@Entity
@Table(name = "courier")
@Data
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private CourierStatus status = CourierStatus.WAITING;

    @PersistenceCreator
    public Courier() {
    }

}
