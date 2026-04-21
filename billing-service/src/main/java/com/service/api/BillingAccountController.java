package com.service.api;

import com.service.database.Account;
import com.service.service.BillingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер биллинга. В этом контроллере все запросы требуют прохождения аутентификации, которая реализована через
 * сервис auth-сервис.
 */
@RestController
@RequestMapping("/api/billing/v1")
@RequiredArgsConstructor
public class BillingAccountController {

    private final BillingAccountService accountService;

    /**
     * Получить аккаунт.
     */
    @GetMapping("/account")
    public ResponseEntity<Account> getAccount(@RequestParam(value = "userId") String userId) {
        Account account = accountService.getAccount(userId);
        return ResponseEntity.status(HttpStatus.OK).body(account);

    }

    /**
     * Увеличить баланс аккаунта.
     */
    @PostMapping("/account/deposit")
    public ResponseEntity<String> depositAccount(@RequestBody AccountRequest request) {
        boolean deposited = accountService.depositAccount(request.getUserId(), request.getAmount());
        if (deposited) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Not deposited");
    }

    /**
     * Уменьшить баланс аккаунта.
     */
    @PostMapping("/account/withdraw")
    public ResponseEntity<String> withdrawAccount(@RequestBody AccountRequest request) {

        boolean withdrawn = accountService.withdrawAccount(request.getUserId(), request.getAmount());
        if (withdrawn) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Not withdrawn");
    }

}
