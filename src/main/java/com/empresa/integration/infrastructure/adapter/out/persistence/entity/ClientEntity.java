package com.empresa.integration.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/** Entidad JPA que mapea la tabla clients. */
@Entity
@Table(name = "clients")
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    @Column(nullable = false)
    private String email;

    @Column(name = "legacy_id")
    private String legacyId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Constructor sin argumentos requerido por JPA. */
    protected ClientEntity() {
    }

    /** Constructor completo para crear o reconstruir la entidad. */
    public ClientEntity(UUID id, String name, String documentNumber,
            String email, String legacyId,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.documentNumber = documentNumber;
        this.email = email;
        this.legacyId = legacyId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** @return identificador unico */
    public UUID getId() {
        return id;
    }

    /** @return nombre del cliente */
    public String getName() {
        return name;
    }

    /** @return numero de documento */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /** @return correo electronico */
    public String getEmail() {
        return email;
    }

    /** @return identificador en sistema legacy */
    public String getLegacyId() {
        return legacyId;
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
