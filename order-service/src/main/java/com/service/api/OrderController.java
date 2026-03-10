package com.service.api;

import com.service.database.Order;
import com.service.service.OrderManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderManagerService orderManagerService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> met() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "OK"));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest req) {
        Optional<Order> order1 = orderManagerService.createOrder(req.getUserId(), req.getAmount());
        return ResponseEntity.status(HttpStatus.OK).body("Order created");
    }

}
