package com.empresa.integration.infrastructure.adapter.in.web;

import com.empresa.integration.application.port.in.ClientQueryUseCase;
import com.empresa.integration.application.port.out.TransactionRepository;
import com.empresa.integration.infrastructure.adapter.in.web.dto.ClientResponse;
import com.empresa.integration.infrastructure.adapter.in.web.dto.PagedResponse;
import com.empresa.integration.infrastructure.adapter.in.web.dto.TransactionResponse;
import com.empresa.integration.infrastructure.adapter.in.web.mapper.TransactionWebMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Endpoints REST para consulta de clientes y sus transacciones. */
@RestController
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients", description = "Consulta de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientQueryUseCase clientQueryUseCase;
    private final TransactionRepository transactionRepository;
    private final TransactionWebMapper mapper;

    public ClientController(ClientQueryUseCase clientQueryUseCase,
                            TransactionRepository transactionRepository,
                            TransactionWebMapper mapper) {
        this.clientQueryUseCase = clientQueryUseCase;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<ClientResponse> findById(@PathVariable UUID id) {
        var client = clientQueryUseCase.findById(id);
        return ResponseEntity.ok(new ClientResponse(
            client.getId(), client.getName(),
            client.getDocumentNumber(), client.getEmail(), client.getLegacyId()));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Listar transacciones del cliente")
    public PagedResponse<TransactionResponse> findTransactions(
            @PathVariable UUID id, Pageable pageable) {
        clientQueryUseCase.findById(id);
        return PagedResponse.from(
            transactionRepository.findByClientId(id, pageable).map(mapper::toResponse));
    }
}
