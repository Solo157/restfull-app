package com.service.database;

import com.service.saga.KeyIdempotence;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.stereotype.Indexed;

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
    private Integer amount;
    private KeyIdempotence keyIdempotence;

    /**
     * Оптимистическая блокировка. Если два потока пытаются обновить одну строку одновременно,
     * один из них получит OptimisticLockException.
     */
    @Version
    private Long version;

    @CreatedDate
    private Date date;

    @PersistenceCreator
    public ProcessedCommand() {
    }

}
