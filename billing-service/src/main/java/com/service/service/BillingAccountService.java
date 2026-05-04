package com.service.service;

import com.service.database.Account;
import com.service.database.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Главный сервис по работе с биллингом.
 */
@Service
@RequiredArgsConstructor
public class BillingAccountService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Account getAccount(String userId) {
        Optional<Account> userAccountOpt = accountRepository.findByUserId(userId);
        return userAccountOpt.orElse(null);
    }

    /**
     * Создать аккаунт для пользователя.
     */
    @Transactional
    public void createAccount(String userId) {
        Account account = new Account();
        account.setUserId(userId);
        account.setAmount(0);
        accountRepository.save(account);
    }

    /**
     * Положить деньги на аккаунт.
     */
    @Transactional
    public boolean depositAccount(String userId, Integer amount) {
        Optional<Account> userAccountOpt = accountRepository.findByUserId(userId);
        if (userAccountOpt.isEmpty()) {
            return false;
        }
        Account userAccount = userAccountOpt.get();

        userAccount.setAmount(userAccount.getAmount() + amount);
        accountRepository.save(userAccount);
        return true;
    }

    /**
     * Снять деньги с аккаунта.
     */
    @Transactional
    public boolean withdrawAccount(String userId, Integer amount) {
        Optional<Account> userAccountOpt = accountRepository.findByUserId(userId);
        if (userAccountOpt.isEmpty()) {
            return false;
        }
        Account userAccount = userAccountOpt.get();

        Integer currentAmount = userAccount.getAmount();
        int diffAmounts = currentAmount - amount;
        if (diffAmounts < 0) {
            return false;
        }

        userAccount.setAmount(diffAmounts);
        System.out.println("withdrawAccount: " + userAccount);
        accountRepository.save(userAccount);
        return true;
    }

}
