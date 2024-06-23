package com.github.marinaks.payment.controller;

import com.github.marinaks.payment.dto.BalanceDto;
import com.github.marinaks.payment.dto.PaymentDto;
import com.github.marinaks.payment.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "api/v1/wallet")
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.OK)
    public void changeBalance(@Valid @RequestBody PaymentDto paymentDto) {
        walletService.changeBalance(paymentDto);
    }

    @GetMapping("/{walletId}")
    public BalanceDto getBalance(@PathVariable UUID walletId) {
        return walletService.getBalance(walletId);
    }
}
