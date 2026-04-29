package com.service.service;

import com.service.adapter.DistributedAdapterSender;
import com.service.adapter.events.OrderCreatedEvent;
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

    private final DistributedAdapterSender distributedAdapterSender;
    private final AccountRepository accountRepository;

    /**
     * Проверка хватает ли средств на аккаунте для суммы ордера.
     */
    public boolean checkAccountAmount(String userId, Integer orderAmount) {
        Optional<Account> accountOpt = accountRepository.findByUserId(userId);
        return accountOpt
                .filter(account -> orderAmount <= account.getAmount())
                .isPresent();
    }

    public Account getAccount(String userId) {
        Optional<Account> userAccountOpt = accountRepository.findByUserId(userId);
        return userAccountOpt.orElse(null);
    }

    /**
     * Создать аккаунт для пользователя.
     */
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

    /**
     * Обработка приходящего ивента о том, что заказ создан. Нужно снять деньги с аккаунта за заказ.
     */
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        System.out.println("handleOrderStatusEvent: " + event);

        boolean withdrawAccount = withdrawAccount(event.getUserId(), event.getAmount());
        if (withdrawAccount) {
            distributedAdapterSender.sendOrderPaymentSucceededEvent(event.getUserId(), event.getOrderId());
            return;
        }

        distributedAdapterSender.sendOrderPaymentNoMoneyEvent(event.getUserId(), event.getOrderId());
    }

    /**
     * Обработка приходящего ивента о том, что заказ создан. Нужно снять деньги с аккаунта за заказ.
     */
    public void releaseUserMoney(String userId, Integer amount) {
        System.out.println("releaseUserMoney: " + event);

        depositAccount(userId, amount);

        distributedAdapterSender.sendOrderPaymentSucceededEvent(event.getUserId(), event.getOrderId());

        distributedAdapterSender.sendOrderPaymentNoMoneyEvent(event.getUserId(), event.getOrderId());
    }

}
