package com.empresa.integration.infrastructure.adapter.out.persistence;

import com.empresa.integration.domain.model.Client;
import com.empresa.integration.infrastructure.adapter.out.persistence.entity.ClientEntity;
import com.empresa.integration.infrastructure.adapter.out.persistence.mapper.ClientPersistenceMapperImpl;
import com.empresa.integration.infrastructure.adapter.out.persistence.repository.ClientJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientJpaAdapterTest {

    @Mock
    private ClientJpaRepository jpaRepository;

    private ClientJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ClientJpaAdapter(jpaRepository, new ClientPersistenceMapperImpl());
    }

    @Test
    void findById_returnsClientWhenExists() {
        UUID id = UUID.randomUUID();
        ClientEntity entity = new ClientEntity(id, "Pedro Lopez", "DOC-999",
            "pedro@empresa.com", "LEG-001", LocalDateTime.now(), LocalDateTime.now());
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Client> result = adapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getName()).isEqualTo("Pedro Lopez");
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Client> result = adapter.findById(id);

        assertThat(result).isEmpty();
    }
}
