package com.github.marinaks.payment.service;

import com.github.marinaks.payment.dto.BalanceDto;
import com.github.marinaks.payment.dto.PaymentDto;
import com.github.marinaks.payment.exception.ConcurrentModificationException;
import com.github.marinaks.payment.exception.InsufficientFundsException;
import com.github.marinaks.payment.exception.ObjectNotFoundException;
import com.github.marinaks.payment.mapper.WalletMapper;
import com.github.marinaks.payment.model.OperationType;
import com.github.marinaks.payment.model.Payment;
import com.github.marinaks.payment.model.Wallet;
import com.github.marinaks.payment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

    private final TransactionTemplate transactionTemplate;

    @Override
    public void changeBalance(PaymentDto paymentDto) {
        Payment payment = WalletMapper.toPayment(paymentDto);
        Wallet wallet = walletRepository.findById(payment.getWalletId()).orElseThrow(
                () -> new ObjectNotFoundException("Такого кошелька не существует")
        );
        int retries = 5;
        while (retries > 0) {
            try {
                transactionTemplate.execute((transactionStatus) -> {
                    if (paymentDto.getOperationType() == OperationType.DEPOSIT) {
                        wallet.setBalance(wallet.getBalance() + payment.getAmount());
                    }
                    if (paymentDto.getOperationType() == OperationType.WITHDRAW) {
                        if (wallet.getBalance() < payment.getAmount()) {
                            throw new InsufficientFundsException("Недостаточно средств");
                        }
                        wallet.setBalance(wallet.getBalance() - payment.getAmount());
                    }
                    walletRepository.save(wallet);
                    return null;
                });
                return;
            } catch (OptimisticLockingFailureException ex) {
                retries--;
                if (retries == 0) {
                    throw new ConcurrentModificationException("Конкуретные изменения при обработке запроса");
                }
            }
        }
    }

    @Override
    public BalanceDto getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(
                () -> new ObjectNotFoundException("Такого кошелька не существует")
        );
        return WalletMapper.toBalanceDto(wallet);
    }
}
