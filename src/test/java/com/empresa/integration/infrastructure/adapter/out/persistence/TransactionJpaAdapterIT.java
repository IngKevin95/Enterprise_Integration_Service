package com.empresa.integration.infrastructure.adapter.out.persistence;

import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.domain.model.TransactionStatus;
import com.empresa.integration.infrastructure.adapter.out.persistence.entity.ClientEntity;
import com.empresa.integration.infrastructure.adapter.out.persistence.mapper.TransactionPersistenceMapperImpl;
import com.empresa.integration.infrastructure.adapter.out.persistence.repository.ClientJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TransactionJpaAdapter.class, TransactionPersistenceMapperImpl.class,
    FlywayAutoConfiguration.class})
@ActiveProfiles("test")
class TransactionJpaAdapterIT {

    @Autowired
    private TransactionJpaAdapter adapter;

    @Autowired
    private ClientJpaRepository clientJpaRepository;

    private UUID existingClientId;

    @BeforeEach
    void setUp() {
        ClientEntity client = new ClientEntity(
            UUID.randomUUID(), "Cliente Test", "DOC-" + UUID.randomUUID(),
            "test@empresa.com", null,
            LocalDateTime.now(), LocalDateTime.now());
        existingClientId = clientJpaRepository.save(client).getId();
    }

    private Transaction buildTransaction() {
        return new Transaction(
            null,
            existingClientId,
            new BigDecimal("150.00"),
            "transaccion de prueba",
            TransactionStatus.PENDING,
            LocalDateTime.now(),
            LocalDateTime.now());
    }

    @Test
    void save_persistsAndReturnsTransactionWithId() {
        Transaction saved = adapter.save(buildTransaction());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getClientId()).isEqualTo(existingClientId);
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void findById_returnsTransactionWhenExists() {
        Transaction saved = adapter.save(buildTransaction());

        Optional<Transaction> found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void findById_returnsEmptyWhenNotExists() {
        Optional<Transaction> found = adapter.findById(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsPaginatedResults() {
        adapter.save(buildTransaction());
        adapter.save(buildTransaction());

        Page<Transaction> page = adapter.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void findByClientId_returnsOnlyMatchingClient() {
        UUID otherId = clientJpaRepository.save(new ClientEntity(
            UUID.randomUUID(), "Otro Cliente", "DOC-OTHER-" + UUID.randomUUID(),
            "otro@empresa.com", null, LocalDateTime.now(), LocalDateTime.now())).getId();

        adapter.save(buildTransaction());
        adapter.save(new Transaction(null, otherId, new BigDecimal("50.00"),
            "otro", TransactionStatus.PENDING, LocalDateTime.now(), LocalDateTime.now()));

        Page<Transaction> page = adapter.findByClientId(existingClientId, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent()).allMatch(t -> t.getClientId().equals(existingClientId));
    }
}
