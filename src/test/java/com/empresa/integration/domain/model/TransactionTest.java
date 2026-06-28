package com.empresa.integration.domain.model;

import com.empresa.integration.domain.exception.InvalidStatusTransitionException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    private Transaction buildTransaction(TransactionStatus status) {
        return new Transaction(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("100.00"),
            "test",
            status,
            LocalDateTime.now(),
            LocalDateTime.now());
    }

    @Test
    void changeStatus_fromPendingToProcessing_succeeds() {
        Transaction tx = buildTransaction(TransactionStatus.PENDING);
        tx.changeStatus(TransactionStatus.PROCESSING);
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.PROCESSING);
    }

    @Test
    void changeStatus_fromTerminal_throwsException() {
        Transaction tx = buildTransaction(TransactionStatus.COMPLETED);
        assertThatThrownBy(() -> tx.changeStatus(TransactionStatus.FAILED))
            .isInstanceOf(InvalidStatusTransitionException.class)
            .hasMessageContaining("Cannot transition from COMPLETED to FAILED");
    }

    @Test
    void changeStatus_updatesUpdatedAt() throws InterruptedException {
        Transaction tx = buildTransaction(TransactionStatus.PENDING);
        LocalDateTime before = tx.getUpdatedAt();
        Thread.sleep(5);
        tx.changeStatus(TransactionStatus.PROCESSING);
        assertThat(tx.getUpdatedAt()).isAfter(before);
    }

    @Test
    void getters_returnCorrectValues() {
        UUID id = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200.00");
        LocalDateTime now = LocalDateTime.now();
        Transaction tx = new Transaction(id, clientId, amount, "desc", TransactionStatus.PENDING, now, now);
        assertThat(tx.getId()).isEqualTo(id);
        assertThat(tx.getClientId()).isEqualTo(clientId);
        assertThat(tx.getAmount()).isEqualTo(amount);
        assertThat(tx.getDescription()).isEqualTo("desc");
        assertThat(tx.getCreatedAt()).isEqualTo(now);
    }
}
