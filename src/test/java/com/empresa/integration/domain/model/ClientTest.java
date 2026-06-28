package com.empresa.integration.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClientTest {

    @Test
    void constructor_setsAllFields() {
        UUID id = UUID.randomUUID();
        Client client = new Client(id, "Juan Perez", "12345678", "juan@email.com", "LEG-001");

        assertThat(client.getId()).isEqualTo(id);
        assertThat(client.getName()).isEqualTo("Juan Perez");
        assertThat(client.getDocumentNumber()).isEqualTo("12345678");
        assertThat(client.getEmail()).isEqualTo("juan@email.com");
        assertThat(client.getLegacyId()).isEqualTo("LEG-001");
    }

    @Test
    void constructor_allowsNullLegacyId() {
        Client client = new Client(UUID.randomUUID(), "Ana", "99", "ana@x.com", null);
        assertThat(client.getLegacyId()).isNull();
    }
}
