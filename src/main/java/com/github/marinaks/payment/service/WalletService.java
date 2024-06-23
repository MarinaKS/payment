package com.github.marinaks.payment.service;

import com.github.marinaks.payment.dto.BalanceDto;
import com.github.marinaks.payment.dto.PaymentDto;

import java.util.UUID;

public interface WalletService {
    void changeBalance(PaymentDto paymentDto);
    BalanceDto getBalance(UUID walletId);
}
