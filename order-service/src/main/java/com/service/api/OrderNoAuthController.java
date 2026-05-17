package com.service.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Контроллер, ендпоинты которого не требуют аутентификацию. Используется для внутренних вызовов.
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderNoAuthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "OK"));
    }

}
