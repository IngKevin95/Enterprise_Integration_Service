package com.empresa.integration.infrastructure.adapter.out.persistence.repository;

import com.empresa.integration.infrastructure.adapter.out.persistence.entity.TransactionEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Repositorio Spring Data JPA para transacciones. */
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    Page<TransactionEntity> findByClientId(UUID clientId, Pageable pageable);
}
