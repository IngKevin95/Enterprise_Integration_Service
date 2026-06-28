package com.empresa.integration.domain.exception;

import com.empresa.integration.domain.model.TransactionStatus;

/** Excepcion de dominio: transicion de estado invalida para una transaccion. */
public class InvalidStatusTransitionException extends BusinessRuleException {

    /**
     * Crea una excepcion describiendo la transicion invalida.
     *
     * @param from estado actual
     * @param to estado destino rechazado
     */
    public InvalidStatusTransitionException(TransactionStatus from, TransactionStatus to) {
        super("Cannot transition from " + from + " to " + to);
    }
}
