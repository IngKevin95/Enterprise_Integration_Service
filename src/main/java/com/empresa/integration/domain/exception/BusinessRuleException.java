package com.empresa.integration.domain.exception;

/** Excepcion de dominio: regla de negocio violada. */
public class BusinessRuleException extends RuntimeException {

    /**
     * Crea una excepcion con la descripcion de la regla violada.
     *
     * @param message descripcion de la violation
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
