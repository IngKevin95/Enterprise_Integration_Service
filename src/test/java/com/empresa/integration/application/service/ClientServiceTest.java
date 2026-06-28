package com.empresa.integration.application.service;

import com.empresa.integration.application.port.out.ClientRepository;
import com.empresa.integration.domain.exception.NotFoundException;
import com.empresa.integration.domain.model.Client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientService = new ClientService(clientRepository);
    }

    private Client sampleClient(UUID id) {
        return new Client(id, "Empresa Test", "DOC-123", "test@empresa.com", "LEG-001");
    }

    @Test
    void findById_returnsClientWhenExists() {
        UUID id = UUID.randomUUID();
        Client client = sampleClient(id);
        when(clientRepository.findById(id)).thenReturn(Optional.of(client));

        Client result = clientService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Empresa Test");
    }

    @Test
    void findById_throwsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(id))
            .isInstanceOf(NotFoundException.class);
    }
}
