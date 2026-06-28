package com.empresa.integration.infrastructure.adapter.out.persistence;

import com.empresa.integration.application.port.out.ClientRepository;
import com.empresa.integration.domain.model.Client;
import com.empresa.integration.infrastructure.adapter.out.persistence.mapper.ClientPersistenceMapper;
import com.empresa.integration.infrastructure.adapter.out.persistence.repository.ClientJpaRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/** Adaptador JPA para el puerto de salida ClientRepository. */
@Component
public class ClientJpaAdapter implements ClientRepository {

    private final ClientJpaRepository jpaRepository;
    private final ClientPersistenceMapper mapper;

    /** Constructor con sus dependencias. */
    public ClientJpaAdapter(ClientJpaRepository jpaRepository, ClientPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
