package com.empresa.integration.application.service;

import com.empresa.integration.application.port.out.TransactionRepository;
import com.empresa.integration.domain.exception.NotFoundException;
import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.domain.model.TransactionStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService(transactionRepository);
    }

    private Transaction sampleTransaction(UUID id, TransactionStatus status) {
        return new Transaction(id, UUID.randomUUID(), new BigDecimal("100.00"),
            "desc", status, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void findAll_delegatesToRepository() {
        Page<Transaction> page = new PageImpl<>(List.of(sampleTransaction(UUID.randomUUID(),
            TransactionStatus.PENDING)));
        when(transactionRepository.findAll(any())).thenReturn(page);

        Page<Transaction> result = service.findAll(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findById_returnsTransactionWhenExists() {
        UUID id = UUID.randomUUID();
        Transaction tx = sampleTransaction(id, TransactionStatus.PENDING);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));

        Transaction result = service.findById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_throwsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_savesNewTransactionWithPendingStatus() {
        UUID clientId = UUID.randomUUID();
        Transaction saved = sampleTransaction(UUID.randomUUID(), TransactionStatus.PENDING);
        when(transactionRepository.save(any())).thenReturn(saved);

        Transaction result = service.create(clientId, new BigDecimal("200.00"), "nueva");

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void updateStatus_changesStatusAndSaves() {
        UUID id = UUID.randomUUID();
        Transaction tx = sampleTransaction(id, TransactionStatus.PENDING);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any())).thenReturn(tx);

        Transaction result = service.updateStatus(id, TransactionStatus.PROCESSING);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PROCESSING);
    }

    @Test
    void findByClientId_delegatesToRepository() {
        UUID clientId = UUID.randomUUID();
        Page<Transaction> page = new PageImpl<>(List.of(
            sampleTransaction(UUID.randomUUID(), TransactionStatus.COMPLETED)));
        when(transactionRepository.findByClientId(eq(clientId), any())).thenReturn(page);

        Page<Transaction> result = service.findByClientId(clientId, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
