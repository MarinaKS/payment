package com.github.marinaks.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class BalanceDto {
    private UUID walletId;
    private Long balance;
}
