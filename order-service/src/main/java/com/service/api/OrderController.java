package com.service.api;

import com.service.api.dto.OrderDTO;
import com.service.api.dto.OrderResponse;
import com.service.api.dto.OrderUpdateDTO;
import com.service.database.Order;
import com.service.service.IdempotencyStorageService;
import com.service.service.OrderManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Контроллер по работе с ордерами. Данный контроллер требует аутентификацию.
 */
@RestController
@RequestMapping("/api/order/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderManagerService orderManagerService;
    private final IdempotencyStorageService idempotencyStorageService;

    /**
     * Получить заказ пользователя.
     */
    @GetMapping
    public ResponseEntity<?> getOrders(@RequestParam String userId) {
        List<OrderDTO> orderDTOs = orderManagerService.getOrderDTOs(userId);
        return ResponseEntity.status(HttpStatus.OK).body(orderDTOs);
    }

    /**
     * Создать заказ для пользователя.
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader String xRequestId, @RequestBody OrderDTO orderDTO) {

        // нет идентификатора, значит запрос некорректный
        if (xRequestId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("X-Request-Id not sent");
        }

        // 1. атомарная регистрация по ключу идемпотентности
        boolean isKeyFirst = idempotencyStorageService.tryStart(xRequestId);

        // если уже есть ключ, то возвращаем состояние заказа (его идентификатор, если есть, и статус)
        if (!isKeyFirst) {
            OrderResponse responseByRequestId = idempotencyStorageService.getResponseByRequestId(xRequestId);
            return ResponseEntity.ok(responseByRequestId);
        }

        // 2. создаём заказ
        Order order = orderManagerService.createOrder(orderDTO)
                .orElseThrow();

        // 3. сохраняем результат
        idempotencyStorageService.updateOrderResponseByRequestId(xRequestId, order);

        return ResponseEntity.ok(idempotencyStorageService.getResponseByRequestId(xRequestId));
    }

    /**
     * Обновить заказ для пользователя.
     */
    @PatchMapping("/{orderId}")
    public ResponseEntity<String> updateOrder(@PathVariable String orderId, @RequestBody OrderUpdateDTO orderDTO) {
        // нет идентификатора, значит запрос некорректный
        if (orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OrderId not sent");
        }

        Order order;
        try {
            order = orderManagerService.updateOrder(orderId, orderDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Order not created. Order version incorrect");
        }

        if (order == null) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Order not found for update");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Order updated");
    }

}
