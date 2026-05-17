package com.service.database;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.*;

@Entity
@Table(name = "processedCommand")
@Data
public class ProcessedCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID sagaId;
    private Long orderId;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @CreatedDate
    private Date date;

    @PersistenceCreator
    public ProcessedCommand() {
    }

}
