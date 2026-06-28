package com.empresa.integration.infrastructure.adapter.in.web.dto;

import com.empresa.integration.domain.model.TransactionStatus;

import jakarta.validation.constraints.NotNull;

/** DTO para actualizar el estado de una transaccion. */
public record UpdateStatusRequest(
    @NotNull(message = "El estado es obligatorio")
    TransactionStatus status
) {
}
