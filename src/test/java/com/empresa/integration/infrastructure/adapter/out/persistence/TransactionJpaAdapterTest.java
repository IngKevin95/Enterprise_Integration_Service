package com.empresa.integration.infrastructure.adapter.out.persistence;

import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.domain.model.TransactionStatus;
import com.empresa.integration.infrastructure.adapter.out.persistence.entity.TransactionEntity;
import com.empresa.integration.infrastructure.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import com.empresa.integration.infrastructure.adapter.out.persistence.repository.TransactionJpaRepository;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionJpaAdapterTest {

    @Mock
    private TransactionJpaRepository jpaRepository;
    @Mock
    private TransactionPersistenceMapper mapper;

    private TransactionJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TransactionJpaAdapter(jpaRepository, mapper);
    }

    private Transaction sampleDomain() {
        return new Transaction(UUID.randomUUID(), UUID.randomUUID(),
            new BigDecimal("100.00"), "desc",
            TransactionStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());
    }

    private TransactionEntity sampleEntity() {
        return new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(),
            new BigDecimal("100.00"), "desc",
            TransactionStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void save_mapsAndPersists() {
        Transaction domain = sampleDomain();
        TransactionEntity entity = sampleEntity();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        Transaction result = adapter.save(domain);

        assertThat(result).isEqualTo(domain);
    }

    @Test
    void findById_returnsMappedDomainWhenExists() {
        UUID id = UUID.randomUUID();
        Transaction domain = sampleDomain();
        TransactionEntity entity = sampleEntity();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<Transaction> result = adapter.findById(id);

        assertThat(result).contains(domain);
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(adapter.findById(id)).isEmpty();
    }

    @Test
    void findAll_returnsMappedPage() {
        Transaction domain = sampleDomain();
        TransactionEntity entity = sampleEntity();
        when(jpaRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Page<Transaction> result = adapter.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(domain);
    }

    @Test
    void findByClientId_returnsMappedPage() {
        UUID clientId = UUID.randomUUID();
        Transaction domain = sampleDomain();
        TransactionEntity entity = sampleEntity();
        when(jpaRepository.findByClientId(eq(clientId), any()))
            .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Page<Transaction> result = adapter.findByClientId(clientId, PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(domain);
    }
}
