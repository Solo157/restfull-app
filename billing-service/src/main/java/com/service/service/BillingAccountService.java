package com.service.service;

import com.service.adapter.OrderStatusEventDTO;
import com.service.adapter.PaymentStatus;
import com.service.adapter.RabbitAdapterService;
import com.service.database.Account;
import com.service.database.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BillingAccountService {

    private final RabbitAdapterService rabbitAdapterService;
    private final AccountRepository accountRepository;

    public boolean checkAccountAmount(String userId, String orderId, Long orderAmount) {
        Optional<Account> accountOpt = accountRepository.findByUserId(userId);
        if (accountOpt.isEmpty()) {
            rabbitAdapterService.sendOrderNoMoneyEvent(userId, orderId, orderAmount);
            return false;
        }
        Account account = accountOpt.get();
        Long accountAmount = account.getAmount();

        if (orderAmount > accountAmount) {
            rabbitAdapterService.sendOrderNoMoneyEvent(userId, orderId, orderAmount);
            return false;
        }

        return true;
    }

    public void createAccount(String userId) {
        Account account = new Account();
        account.setUserId(userId);
        account.setAmount(0L);
        accountRepository.save(account);
    }

    public boolean depositAccount(String userId, Long amount) {
        Optional<Account> userAccountOpt = accountRepository.findByUserId(userId);
        if (userAccountOpt.isEmpty()) {
            return false;
        }
        Account userAccount = userAccountOpt.get();

        userAccount.setAmount(userAccount.getAmount() + amount);
        accountRepository.save(userAccount);
        return true;
    }

    public boolean withdrawAccount(String userId, Long amount) {
        Optional<Account> userAccountOpt = accountRepository.findByUserId(userId);
        if (userAccountOpt.isEmpty()) {
            return false;
        }
        Account userAccount = userAccountOpt.get();

        Long currentAmount = userAccount.getAmount();
        if ((currentAmount - amount) < 0) {
            return false;
        }

        userAccount.setAmount(currentAmount - amount);
        accountRepository.save(userAccount);
        return true;
    }

    public void handleOrderStatusEvent(OrderStatusEventDTO event) {
        PaymentStatus paymentStatus = event.getPaymentStatus();
        if (paymentStatus == PaymentStatus.PENDING) {
            paymentStatus = PaymentStatus.NOT_PAID;
            boolean withdrawAccount = withdrawAccount(event.getUserId(), event.getAmount());
            if (withdrawAccount) {
                paymentStatus = PaymentStatus.PAID;
            }
            rabbitAdapterService.sendOrderPaymentStatusEvent(
                    event.getUserId(),
                    event.getOrderId(),
                    event.getOrderStatus(),
                    paymentStatus,
                    event.getAmount()
            );
        }
    }

}
