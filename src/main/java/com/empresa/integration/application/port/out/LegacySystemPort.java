package com.empresa.integration.application.port.out;

import com.empresa.integration.domain.model.Client;

import java.util.Optional;

/** Puerto de salida para integracion con el sistema legacy. */
public interface LegacySystemPort {

    /**
     * Busca un cliente en el sistema legacy por su identificador legacy.
     *
     * @param legacyId identificador del cliente en el sistema legacy
     * @return el cliente si existe en el sistema legacy
     */
    Optional<Client> findByLegacyId(String legacyId);

    /**
     * Verifica si el sistema legacy esta disponible.
     *
     * @return true si el sistema legacy responde
     */
    boolean isAvailable();
}
