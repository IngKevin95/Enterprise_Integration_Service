package com.empresa.integration.domain.model;

import com.empresa.integration.domain.exception.InvalidStatusTransitionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Entidad de dominio que representa una transaccion financiera. */
public class Transaction {

    private final UUID id;
    private final UUID clientId;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor completo para reconstruir desde persistencia.
     *
     * @param id identificador unico
     * @param clientId identificador del cliente
     * @param amount monto de la transaccion
     * @param description descripcion opcional
     * @param status estado actual
     * @param createdAt fecha de creacion
     * @param updatedAt fecha de ultima actualizacion
     */
    public Transaction(
            UUID id,
            UUID clientId,
            BigDecimal amount,
            String description,
            TransactionStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Cambia el estado de la transaccion validando la transicion.
     *
     * @param newStatus nuevo estado
     * @throws InvalidStatusTransitionException si la transicion no es permitida
     */
    public void changeStatus(TransactionStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(this.status, newStatus);
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /** @return identificador unico */
    public UUID getId() {
        return id;
    }

    /** @return identificador del cliente */
    public UUID getClientId() {
        return clientId;
    }

    /** @return monto de la transaccion */
    public BigDecimal getAmount() {
        return amount;
    }

    /** @return descripcion de la transaccion */
    public String getDescription() {
        return description;
    }

    /** @return estado actual */
    public TransactionStatus getStatus() {
        return status;
    }

    /** @return fecha de creacion */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return fecha de ultima actualizacion */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
