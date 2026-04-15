package com.service.database;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.*;

/**
 * Информация о заказе.
 */
@Entity
@Table(name = "orderInfos")
@Data
public class OrderInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    /**
     * Связь "многие к одному". Устанавливается идентификатор notification-а к которому принадлежит заказ.
     */
    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;

    /**
     * Сообщение по заказа. Текст о том, что произошло с заказом.
     */
    private String message;

    /**
     * Сумма заказа.
     */
    private Integer amount;

    /**
     * Вложенная коллекция. Для ордера создается отдельная таблица с его пунктами.
     */
    @ElementCollection
    @CollectionTable(name = "order_itemInfos", joinColumns = @JoinColumn(name = "orderInfo_id"))
    private List<OrderItemInfo> itemInfos;

    private String deliveryAddress;
    private String contactPhone;
    private String orderDate;

    @PersistenceCreator
    public OrderInfo() {
    }

    @Override
    public String toString() {
        return "OrderInfo{" +
                "id=" + id + '}';
    }

}
