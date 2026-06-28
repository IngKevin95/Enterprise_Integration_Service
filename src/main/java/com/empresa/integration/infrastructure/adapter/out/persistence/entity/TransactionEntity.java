package com.empresa.integration.infrastructure.adapter.out.persistence.entity;

import com.empresa.integration.domain.model.TransactionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Entidad JPA que mapea la tabla transactions. */
@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Constructor sin argumentos requerido por JPA. */
    protected TransactionEntity() {
    }

    /** Constructor completo para crear o reconstruir la entidad. */
    public TransactionEntity(UUID id, UUID clientId, BigDecimal amount,
            String description, TransactionStatus status,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** @return identificador unico */
    public UUID getId() {
        return id;
    }

    /** @return id del cliente */
    public UUID getClientId() {
        return clientId;
    }

    /** @return monto de la transaccion */
    public BigDecimal getAmount() {
        return amount;
    }

    /** @return descripcion */
    public String getDescription() {
        return description;
    }

    /** @return estado de la transaccion */
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

    /** Actualiza el estado. */
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    /** Actualiza la fecha de modificacion. */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
