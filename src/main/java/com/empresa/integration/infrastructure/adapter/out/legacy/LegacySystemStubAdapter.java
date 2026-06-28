package com.empresa.integration.infrastructure.adapter.out.legacy;

import com.empresa.integration.application.port.out.LegacySystemPort;
import com.empresa.integration.domain.model.Client;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador stub del sistema legacy.
 * Lee datos de legacy-data.json en classpath.
 * En produccion se reemplazaria por un cliente SOAP/JDBC/REST real.
 */
@Component
public class LegacySystemStubAdapter implements LegacySystemPort {

    private final ObjectMapper objectMapper;
    private List<LegacyClientRecord> legacyClients;

    public LegacySystemStubAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadData() throws IOException {
        var resource = new ClassPathResource("legacy-data.json");
        LegacyClientRecord[] records = objectMapper.readValue(
            resource.getInputStream(), LegacyClientRecord[].class);
        this.legacyClients = Arrays.asList(records);
    }

    @Override
    public Optional<Client> findByLegacyId(String legacyId) {
        return legacyClients.stream()
            .filter(r -> r.legacyId().equals(legacyId))
            .map(r -> new Client(UUID.fromString(r.id()), r.name(),
                r.documentNumber(), r.email(), r.legacyId()))
            .findFirst();
    }

    @Override
    public boolean isAvailable() {
        return legacyClients != null;
    }

    /** Registro interno para deserializar el JSON. */
    record LegacyClientRecord(String legacyId, String id, String name,
                              String documentNumber, String email) {
    }
}
