package com.github.marinaks.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.marinaks.payment.dto.PaymentDto;
import com.github.marinaks.payment.model.OperationType;
import com.github.marinaks.payment.model.Wallet;
import com.github.marinaks.payment.repository.WalletRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class WalletControllerTest {

    public static final UUID TEST_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @BeforeEach
    public void setUp() {
        Wallet wallet = walletRepository.findById(TEST_UUID).orElseGet(
                () -> {
                    Wallet w = new Wallet();
                    w.setWalletId(TEST_UUID);
                    w.setBalance(1000L);
                    return w;
                }
        );
        wallet.setBalance(1000L);
        walletRepository.save(wallet);
    }

    @Test
    public void testChangeBalanceDepositSuccess() throws Exception {
        // Arrange
        PaymentDto paymentDto = PaymentDto.builder()
                .walletId(TEST_UUID)
                .operationType(OperationType.DEPOSIT)
                .amount(100L)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentDtoJson = objectMapper.writeValueAsString(paymentDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDtoJson))
                .andExpect(status().isOk());
        Wallet updatedWallet = walletRepository.findById(TEST_UUID).orElseThrow();
        assertEquals(1100L, updatedWallet.getBalance());

        // Повторное обращение к базе данных для проверки фактического баланса
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDtoJson))
                .andExpect(status().isOk());
        Wallet updatedWallet2 = walletRepository.findById(TEST_UUID).orElseThrow();
        assertEquals(1200L, updatedWallet2.getBalance());
    }

    @Test
    public void testChangeBalanceWithdrawSuccess() throws Exception {
        // Arrange
        PaymentDto paymentDto = PaymentDto.builder()
                .walletId(TEST_UUID)
                .operationType(OperationType.WITHDRAW)
                .amount(50L)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentDtoJson = objectMapper.writeValueAsString(paymentDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDtoJson))
                .andExpect(status().isOk());
        Wallet updatedWallet = walletRepository.findById(TEST_UUID).orElseThrow();
        assertEquals(950L, updatedWallet.getBalance());

        // Повторное обращение к базе данных для проверки фактического баланса
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDtoJson))
                .andExpect(status().isOk());
        Wallet updatedWallet2 = walletRepository.findById(TEST_UUID).orElseThrow();
        assertEquals(900L, updatedWallet2.getBalance());
    }

    @Test
    public void testChangeBalanceWalletNotFound() throws Exception {
        // Arrange
        PaymentDto paymentDto = PaymentDto.builder()
                .walletId(UUID.randomUUID())
                .operationType(OperationType.DEPOSIT)
                .amount(100L)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentDtoJson = objectMapper.writeValueAsString(paymentDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDtoJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testChangeBalanceInsufficientFunds() throws Exception {
        // Arrange
        PaymentDto paymentDto = PaymentDto.builder()
                .walletId(TEST_UUID)
                .operationType(OperationType.WITHDRAW)
                .amount(2000L) // Больше, чем текущий баланс
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentDtoJson = objectMapper.writeValueAsString(paymentDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDtoJson))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetBalanceSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/wallet/{walletId}", TEST_UUID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(TEST_UUID.toString()))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    public void testGetBalance_WalletNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/wallet/{walletId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testChangeBalance_ValidationFailed() throws Exception {
        // Arrange
        PaymentDto paymentDto = PaymentDto.builder().build(); // Не установлены обязательные поля
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentDtoJson = objectMapper.writeValueAsString(paymentDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentDtoJson))
                .andExpect(status().isBadRequest());
    }
}
