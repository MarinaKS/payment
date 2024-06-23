package com.github.marinaks.payment.mapper;

import com.github.marinaks.payment.dto.BalanceDto;
import com.github.marinaks.payment.dto.PaymentDto;
import com.github.marinaks.payment.model.Payment;
import com.github.marinaks.payment.model.Wallet;

public class WalletMapper {
    public static Payment toPayment(PaymentDto paymentDto) {
        Payment payment = new Payment();
        payment.setWalletId(paymentDto.getWalletId());
        payment.setAmount(paymentDto.getAmount());
        payment.setOperationType(paymentDto.getOperationType());
        return payment;
    }

    public static BalanceDto toBalanceDto(Wallet wallet) {
        return new BalanceDto(
                wallet.getWalletId(),
                wallet.getBalance()
        );
    }
}
