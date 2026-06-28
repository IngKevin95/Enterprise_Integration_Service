package com.empresa.integration.infrastructure.adapter.out.persistence.repository;

import com.empresa.integration.infrastructure.adapter.out.persistence.entity.ClientEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Repositorio Spring Data JPA para clientes. */
public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {
}
