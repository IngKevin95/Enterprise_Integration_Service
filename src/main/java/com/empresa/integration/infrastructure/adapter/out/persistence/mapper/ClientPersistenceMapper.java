package com.empresa.integration.infrastructure.adapter.out.persistence.mapper;

import com.empresa.integration.domain.model.Client;
import com.empresa.integration.infrastructure.adapter.out.persistence.entity.ClientEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Convierte entre Client (dominio) y ClientEntity (JPA). */
@Mapper(componentModel = "spring")
public interface ClientPersistenceMapper {

    Client toDomain(ClientEntity entity);

    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    ClientEntity toEntity(Client domain);
}
