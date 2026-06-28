package com.empresa.integration.domain.model;

import java.util.UUID;

/** Entidad de dominio que representa un cliente. */
public class Client {

    private final UUID id;
    private String name;
    private String documentNumber;
    private String email;
    private String legacyId;

    /**
     * Constructor completo para reconstruir desde persistencia.
     *
     * @param id identificador unico
     * @param name nombre del cliente
     * @param documentNumber numero de documento unico
     * @param email correo electronico
     * @param legacyId identificador en el sistema legacy
     */
    public Client(UUID id, String name, String documentNumber, String email, String legacyId) {
        this.id = id;
        this.name = name;
        this.documentNumber = documentNumber;
        this.email = email;
        this.legacyId = legacyId;
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
}
