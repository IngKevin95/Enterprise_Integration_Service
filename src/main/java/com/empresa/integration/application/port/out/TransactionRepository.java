package com.empresa.integration.application.port.out;

import com.empresa.integration.domain.model.Transaction;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Puerto de salida para persistencia de transacciones. */
public interface TransactionRepository {

    /**
     * Guarda o actualiza una transaccion.
     *
     * @param transaction la transaccion a persistir
     * @return la transaccion persistida
     */
    Transaction save(Transaction transaction);

    /**
     * Busca una transaccion por identificador.
     *
     * @param id identificador unico
     * @return la transaccion si existe
     */
    Optional<Transaction> findById(UUID id);

    /**
     * Lista todas las transacciones de forma paginada.
     *
     * @param pageable configuracion de paginacion
     * @return pagina de transacciones
     */
    Page<Transaction> findAll(Pageable pageable);

    /**
     * Lista transacciones de un cliente de forma paginada.
     *
     * @param clientId identificador del cliente
     * @param pageable configuracion de paginacion
     * @return pagina de transacciones del cliente
     */
    Page<Transaction> findByClientId(UUID clientId, Pageable pageable);
}
