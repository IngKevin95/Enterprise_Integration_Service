package com.empresa.integration.infrastructure.adapter.in.web;

import com.empresa.integration.application.port.in.TransactionUseCase;
import com.empresa.integration.domain.exception.NotFoundException;
import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.domain.model.TransactionStatus;
import com.empresa.integration.infrastructure.adapter.in.web.mapper.TransactionWebMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionUseCase transactionUseCase;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        TransactionController controller = new TransactionController(
            transactionUseCase, new TransactionWebMapperImpl());
        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    private Transaction sampleTx(UUID id) {
        return new Transaction(id, UUID.randomUUID(), new BigDecimal("100.00"),
            "desc", TransactionStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void findAll_returns200WithPage() throws Exception {
        UUID id = UUID.randomUUID();
        when(transactionUseCase.findAll(any())).thenReturn(
            new PageImpl<>(List.of(sampleTx(id))));

        mockMvc.perform(get("/api/v1/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void findById_returns200WhenExists() throws Exception {
        UUID id = UUID.randomUUID();
        when(transactionUseCase.findById(id)).thenReturn(sampleTx(id));

        mockMvc.perform(get("/api/v1/transactions/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void findById_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(transactionUseCase.findById(id)).thenThrow(new NotFoundException("Transaction", id));

        mockMvc.perform(get("/api/v1/transactions/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_returns201WithBody() throws Exception {
        UUID id = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        when(transactionUseCase.create(eq(clientId), any(), any())).thenReturn(sampleTx(id));

        String body = String.format(
            "{\"clientId\":\"%s\",\"amount\":100.00,\"description\":\"desc\"}", clientId);

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void create_returns400WhenInvalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clientId\":null,\"amount\":null,\"description\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        Transaction updated = new Transaction(id, UUID.randomUUID(), new BigDecimal("100.00"),
            "desc", TransactionStatus.PROCESSING, LocalDateTime.now(), LocalDateTime.now());
        when(transactionUseCase.updateStatus(eq(id), eq(TransactionStatus.PROCESSING)))
            .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/transactions/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PROCESSING\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PROCESSING"));
    }
}
