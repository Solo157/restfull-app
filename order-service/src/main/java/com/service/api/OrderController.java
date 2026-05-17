package com.service.api;

import com.service.api.dto.OrderDTO;
import com.service.database.Order;
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
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO) {
        Optional<Order> orderOpt = orderManagerService.createOrder(orderDTO);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Order not created. Payment required");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Order created");
    }

}
