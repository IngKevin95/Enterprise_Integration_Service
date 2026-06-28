package com.empresa.integration.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionStatusTest {

    @Test
    void pendingCanTransitionToProcessing() {
        assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.PROCESSING)).isTrue();
    }

    @Test
    void pendingCanTransitionToCancelled() {
        assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.CANCELLED)).isTrue();
    }

    @Test
    void pendingCannotTransitionToCompleted() {
        assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.COMPLETED)).isFalse();
    }

    @Test
    void processingCanTransitionToCompleted() {
        assertThat(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.COMPLETED)).isTrue();
    }

    @Test
    void processingCanTransitionToFailed() {
        assertThat(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.FAILED)).isTrue();
    }

    @Test
    void completedIsTerminal() {
        assertThat(TransactionStatus.COMPLETED.isTerminal()).isTrue();
        assertThat(TransactionStatus.COMPLETED.canTransitionTo(TransactionStatus.FAILED)).isFalse();
    }

    @Test
    void failedIsTerminal() {
        assertThat(TransactionStatus.FAILED.isTerminal()).isTrue();
    }

    @Test
    void cancelledIsTerminal() {
        assertThat(TransactionStatus.CANCELLED.isTerminal()).isTrue();
    }

    @Test
    void pendingIsNotTerminal() {
        assertThat(TransactionStatus.PENDING.isTerminal()).isFalse();
    }
}
