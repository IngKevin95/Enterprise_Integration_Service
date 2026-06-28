package com.empresa.integration.infrastructure.adapter.in.web.dto;

import com.empresa.integration.domain.model.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** DTO de respuesta para una transaccion. */
public record TransactionResponse(
    UUID id,
    UUID clientId,
    BigDecimal amount,
    String description,
    TransactionStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
