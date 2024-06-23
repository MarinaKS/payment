package com.github.marinaks.payment.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Payment {
    private UUID walletId;
    private OperationType operationType;
    private Long amount;
}
