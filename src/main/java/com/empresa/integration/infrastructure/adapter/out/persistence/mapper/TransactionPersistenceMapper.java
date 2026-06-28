package com.empresa.integration.infrastructure.adapter.out.persistence.mapper;

import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.infrastructure.adapter.out.persistence.entity.TransactionEntity;

import org.mapstruct.Mapper;

/** Convierte entre Transaction (dominio) y TransactionEntity (JPA). */
@Mapper(componentModel = "spring")
public interface TransactionPersistenceMapper {

    Transaction toDomain(TransactionEntity entity);

    TransactionEntity toEntity(Transaction domain);
}
