package com.empresa.integration.application.service;

import com.empresa.integration.application.port.in.TransactionUseCase;
import com.empresa.integration.application.port.out.TransactionRepository;
import com.empresa.integration.domain.exception.NotFoundException;
import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.domain.model.TransactionStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Implementacion del caso de uso de transacciones. */
@Service
public class TransactionService implements TransactionUseCase {

    private final TransactionRepository transactionRepository;

    /** Constructor con dependencia al repositorio. */
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transaction", id));
    }

    @Override
    @Transactional
    public Transaction create(UUID clientId, BigDecimal amount, String description) {
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = new Transaction(
            null, clientId, amount, description, TransactionStatus.PENDING, now, now);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction updateStatus(UUID id, TransactionStatus newStatus) {
        Transaction transaction = findById(id);
        transaction.changeStatus(newStatus);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findByClientId(UUID clientId, Pageable pageable) {
        return transactionRepository.findByClientId(clientId, pageable);
    }
}
