package com.empresa.integration.application.port.in;

import com.empresa.integration.domain.model.Client;

import java.util.UUID;

/** Puerto de entrada para consultas sobre clientes. */
public interface ClientQueryUseCase {

    /**
     * Busca un cliente por su identificador.
     *
     * @param id identificador del cliente
     * @return el cliente encontrado
     */
    Client findById(UUID id);
}
