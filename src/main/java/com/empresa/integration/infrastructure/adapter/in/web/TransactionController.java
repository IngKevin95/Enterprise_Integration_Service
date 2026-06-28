package com.empresa.integration.infrastructure.adapter.in.web;

import com.empresa.integration.application.port.in.TransactionUseCase;
import com.empresa.integration.infrastructure.adapter.in.web.dto.CreateTransactionRequest;
import com.empresa.integration.infrastructure.adapter.in.web.dto.TransactionResponse;
import com.empresa.integration.infrastructure.adapter.in.web.dto.UpdateStatusRequest;
import com.empresa.integration.infrastructure.adapter.in.web.mapper.TransactionWebMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Endpoints REST para gestion de transacciones. */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Gestion de transacciones")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionUseCase transactionUseCase;
    private final TransactionWebMapper mapper;

    /** Constructor con sus dependencias. */
    public TransactionController(TransactionUseCase transactionUseCase,
            TransactionWebMapper mapper) {
        this.transactionUseCase = transactionUseCase;
        this.mapper = mapper;
    }

    /** Retorna listado paginado de transacciones. */
    @GetMapping
    @Operation(summary = "Listar transacciones")
    public Page<TransactionResponse> findAll(Pageable pageable) {
        return transactionUseCase.findAll(pageable).map(mapper::toResponse);
    }

    /** Retorna el detalle de una transaccion por su ID. */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener transaccion por ID")
    public TransactionResponse findById(@PathVariable UUID id) {
        return mapper.toResponse(transactionUseCase.findById(id));
    }

    /** Crea una nueva transaccion. */
    @PostMapping
    @Operation(summary = "Crear transaccion")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = mapper.toResponse(
            transactionUseCase.create(request.clientId(), request.amount(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Actualiza el estado de una transaccion. */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de transaccion")
    public TransactionResponse updateStatus(@PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return mapper.toResponse(transactionUseCase.updateStatus(id, request.status()));
    }
}
