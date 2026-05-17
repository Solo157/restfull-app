package com.service.api;

import com.service.service.BillingAccountService;
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
public class BillingAccountNoAuthController {

    private final BillingAccountService accountService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "OK"));
    }

    /**
     * Создать аккаунт пользователя.
     */
    @PostMapping("/account")
    public ResponseEntity<String> createAccount(@RequestParam(value = "userId") String userId) {
        System.out.println("createAccount: userId = " + userId);
        accountService.createAccount(userId);
        return ResponseEntity.status(HttpStatus.OK).body("Account registered successfully");
    }

}
