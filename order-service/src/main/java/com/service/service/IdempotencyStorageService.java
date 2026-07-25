package com.service.service;

import com.service.api.dto.OrderResponse;
import com.service.database.Order;
import com.service.database.OrderStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * Класс по регистрации ключа идемпотентности при создании заказа. Ключ генерируется случайным образом на стороне
 * клиента. Ключ хранится определенное количество времени TTL.
 */
@Service
public class IdempotencyStorageService {

    // мапа по хранению ключа идемпотентности и ответа по заказу
    private final Map<String, OrderResponse> requestIds = new ConcurrentHashMap<>();
    // планировщик удаления ключа идемпотентности из мапы
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // время через которое будет удален ключ идемпотетности
    private static final Duration TTL = Duration.ofSeconds(5);

    /**
     * Попробовать начать создание заказа. Если ключ уже создан, то заказ не будет повторно создаваться.
     */
    public boolean tryStart(String requestId) {
        OrderResponse orderResponse = new OrderResponse(null, OrderStatus.IN_PROCESS);

        if (!(requestIds.putIfAbsent(requestId, orderResponse) == null)) {
            return false;
        }

        scheduler.schedule(
                () -> requestIds.remove(requestId),
                TTL.toSeconds(),
                TimeUnit.SECONDS
        );

        return true;
    }

    /**
     * По ключу идемпотентности обновить ответ по заказу.
     */
    public void updateOrderResponseByRequestId(String requestId, Order order) {
        OrderResponse orderResponse = requestIds.get(requestId);
        orderResponse.setOrderId(order.getId().toString());
        orderResponse.setStatus(order.getOrderStatus());
    }

    public OrderResponse getResponseByRequestId(String requestId) {
        return requestIds.get(requestId);
    }

    /**
     * Получить ключ идемпотентности по идентификатору заказа.
     */
    public String getRequestIdByOrderId(String orderId) {
        return requestIds.entrySet().stream()
                .filter(entry -> {
                    String orderIdInMap = entry.getValue().getOrderId();
                    if (orderIdInMap == null) {
                        return false;
                    }

                    return orderIdInMap.equals(orderId);
                })
                .findFirst()
                .get()
                .getKey();
    }

}
