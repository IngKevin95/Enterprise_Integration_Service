package com.empresa.integration.infrastructure.adapter.out.persistence;

import com.empresa.integration.application.port.out.TransactionRepository;
import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.infrastructure.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import com.empresa.integration.infrastructure.adapter.out.persistence.repository.TransactionJpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/** Adaptador JPA para el puerto de salida TransactionRepository. */
@Component
public class TransactionJpaAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionPersistenceMapper mapper;

    /** Constructor con sus dependencias. */
    public TransactionJpaAdapter(
            TransactionJpaRepository jpaRepository,
            TransactionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(transaction)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findByClientId(UUID clientId, Pageable pageable) {
        return jpaRepository.findByClientId(clientId, pageable).map(mapper::toDomain);
    }
}
