package com.github.marinaks.payment.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class Wallet {
    @Id
    private UUID walletId;
    @Version
    private long version;
    private Long balance;
}
