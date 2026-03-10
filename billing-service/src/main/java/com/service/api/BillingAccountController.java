package com.service.api;

import com.service.database.Account;
import com.service.service.BillingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingAccountController {

    private final BillingAccountService accountService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> met() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "OK"));
    }

    @GetMapping("/account/{userId}/amount")
    public ResponseEntity<Void> checkAccountAmount(@PathVariable String userId,
                                                   @RequestParam("orderId") String orderId,
                                                   @RequestParam("orderAmount") Long orderAmount) {
        boolean hasMoney = accountService.checkAccountAmount(userId, orderId, orderAmount);
        if (hasMoney) {
            return ResponseEntity.ok().build(); // 200 OK
        } else {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
    }

    @PostMapping("/account")
    public ResponseEntity<String> createAccount(@RequestParam(value = "userId") String userId) {
        accountService.createAccount(userId);
        return ResponseEntity.status(HttpStatus.OK).body("Account registered successfully");
    }

    @PostMapping("/account/deposit")
    public ResponseEntity<String> depositAccount(@RequestBody AccountRequest request) {

        boolean deposited = accountService.depositAccount(request.getUserId(), request.getAmount());

        if (deposited) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Not deposited");
    }

    @PostMapping("/account/withdraw")
    public ResponseEntity<String> withdrawAccount(@RequestBody AccountRequest request) {

        boolean withdrawn = accountService.withdrawAccount(request.getUserId(), request.getAmount());

        if (withdrawn) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Not withdrawn");
    }

}
