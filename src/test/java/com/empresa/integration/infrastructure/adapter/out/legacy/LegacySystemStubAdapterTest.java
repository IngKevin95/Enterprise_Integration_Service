package com.empresa.integration.infrastructure.adapter.out.legacy;

import com.empresa.integration.domain.model.Client;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LegacySystemStubAdapterTest {

    private LegacySystemStubAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        adapter = new LegacySystemStubAdapter(new ObjectMapper());
        adapter.loadData();
    }

    @Test
    void findByLegacyId_returnsClientWhenExists() {
        Optional<Client> result = adapter.findByLegacyId("LEG-001");

        assertThat(result).isPresent();
        assertThat(result.get().getLegacyId()).isEqualTo("LEG-001");
        assertThat(result.get().getName()).isEqualTo("Empresa Ejemplo S.A.");
    }

    @Test
    void findByLegacyId_returnsEmptyWhenNotExists() {
        Optional<Client> result = adapter.findByLegacyId("LEG-999");

        assertThat(result).isEmpty();
    }

    @Test
    void isAvailable_returnsTrue() {
        assertThat(adapter.isAvailable()).isTrue();
    }
}
