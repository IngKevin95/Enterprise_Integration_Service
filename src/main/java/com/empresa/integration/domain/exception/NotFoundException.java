package com.empresa.integration.domain.exception;

/** Excepcion de dominio: recurso solicitado no existe. */
public class NotFoundException extends RuntimeException {

    /**
     * Crea una excepcion con mensaje descriptivo del recurso no encontrado.
     *
     * @param resourceType tipo del recurso (Transaction, Client, etc.)
     * @param id identificador buscado
     */
    public NotFoundException(String resourceType, Object id) {
        super(resourceType + " with id '" + id + "' does not exist");
    }
}
