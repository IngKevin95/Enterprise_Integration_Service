package com.empresa.integration.infrastructure.adapter.in.web;

import com.empresa.integration.application.port.in.ClientQueryUseCase;
import com.empresa.integration.application.port.out.TransactionRepository;
import com.empresa.integration.domain.exception.NotFoundException;
import com.empresa.integration.domain.model.Client;
import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.domain.model.TransactionStatus;
import com.empresa.integration.infrastructure.adapter.in.web.mapper.TransactionWebMapperImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientQueryUseCase clientQueryUseCase;

    @Mock
    private TransactionRepository transactionRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var controller = new ClientController(
            clientQueryUseCase, transactionRepository, new TransactionWebMapperImpl());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    private Client sampleClient(UUID id) {
        return new Client(id, "Empresa Test", "DOC-123", "test@empresa.com", "LEG-001");
    }

    private Transaction sampleTransaction(UUID clientId) {
        return new Transaction(UUID.randomUUID(), clientId,
            new BigDecimal("200.00"), "pago", TransactionStatus.COMPLETED,
            LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void findById_returns200WithClientData() throws Exception {
        UUID id = UUID.randomUUID();
        when(clientQueryUseCase.findById(id)).thenReturn(sampleClient(id));

        mockMvc.perform(get("/api/v1/clients/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Empresa Test"))
            .andExpect(jsonPath("$.legacyId").value("LEG-001"));
    }

    @Test
    void findById_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(clientQueryUseCase.findById(id)).thenThrow(new NotFoundException("Client", id));

        mockMvc.perform(get("/api/v1/clients/" + id))
            .andExpect(status().isNotFound());
    }

    @Test
    void findTransactions_returns200WithPage() throws Exception {
        UUID id = UUID.randomUUID();
        when(clientQueryUseCase.findById(id)).thenReturn(sampleClient(id));
        when(transactionRepository.findByClientId(eq(id), any()))
            .thenReturn(new PageImpl<>(List.of(sampleTransaction(id)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/clients/" + id + "/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findTransactions_returns404WhenClientNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(clientQueryUseCase.findById(id)).thenThrow(new NotFoundException("Client", id));

        mockMvc.perform(get("/api/v1/clients/" + id + "/transactions"))
            .andExpect(status().isNotFound());
    }
}
