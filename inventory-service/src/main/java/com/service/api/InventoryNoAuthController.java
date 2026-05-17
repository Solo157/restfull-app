package com.service.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Контроллер склада с запросами без аутентификации. Для проверки живо ли приложение не нужна аутентификация.
 * Также тут находятся ендпоинты, которые нужны для запросов между сервисами.
 */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryNoAuthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "OK"));
    }

}
