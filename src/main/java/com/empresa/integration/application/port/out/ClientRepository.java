package com.empresa.integration.application.port.out;

import com.empresa.integration.domain.model.Client;

import java.util.Optional;
import java.util.UUID;

/** Puerto de salida para persistencia de clientes. */
public interface ClientRepository {

    /**
     * Busca un cliente por identificador.
     *
     * @param id identificador unico
     * @return el cliente si existe
     */
    Optional<Client> findById(UUID id);
}
