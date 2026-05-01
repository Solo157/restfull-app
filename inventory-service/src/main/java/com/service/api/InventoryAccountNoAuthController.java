package com.service.api;

import com.service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Контроллер билинга с запросами без аутентификации. Для проверки живо ли приложение не нужна аутентификация.
 * Также тут находятся ендпоинты, которые нужны для запросов между сервисами.
 */
@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class InventoryAccountNoAuthController {

    private final InventoryService accountService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "OK"));
    }

}
