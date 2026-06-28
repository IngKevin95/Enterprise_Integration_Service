package com.empresa.integration.domain.model;

import java.util.Set;

/** Estados posibles de una transaccion y sus transiciones validas. */
public enum TransactionStatus {

    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED;

    private static final Set<TransactionStatus> TERMINAL = Set.of(COMPLETED, FAILED, CANCELLED);

    /**
     * Verifica si la transicion desde este estado al nuevo estado es valida.
     *
     * @param next estado destino
     * @return true si la transicion es permitida
     */
    public boolean canTransitionTo(TransactionStatus next) {
        if (TERMINAL.contains(this)) {
            return false;
        }
        return switch (this) {
            case PENDING -> next == PROCESSING || next == CANCELLED;
            case PROCESSING -> next == COMPLETED || next == FAILED;
            default -> false;
        };
    }

    /**
     * Indica si este estado es terminal (no acepta mas cambios).
     *
     * @return true si el estado es terminal
     */
    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }
}
