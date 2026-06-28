package com.empresa.integration.application.port.in;

import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.domain.model.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Puerto de entrada para operaciones sobre transacciones. */
public interface TransactionUseCase {

    /**
     * Lista todas las transacciones de forma paginada.
     *
     * @param pageable configuracion de paginacion
     * @return pagina de transacciones
     */
    Page<Transaction> findAll(Pageable pageable);

    /**
     * Busca una transaccion por su identificador.
     *
     * @param id identificador de la transaccion
     * @return la transaccion encontrada
     */
    Transaction findById(UUID id);

    /**
     * Crea una nueva transaccion en estado PENDING.
     *
     * @param clientId identificador del cliente
     * @param amount monto de la transaccion
     * @param description descripcion opcional
     * @return la transaccion creada
     */
    Transaction create(UUID clientId, BigDecimal amount, String description);

    /**
     * Cambia el estado de una transaccion existente.
     *
     * @param id identificador de la transaccion
     * @param newStatus nuevo estado
     * @return la transaccion actualizada
     */
    Transaction updateStatus(UUID id, TransactionStatus newStatus);

    /**
     * Lista las transacciones de un cliente especifico.
     *
     * @param clientId identificador del cliente
     * @param pageable configuracion de paginacion
     * @return pagina de transacciones del cliente
     */
    Page<Transaction> findByClientId(UUID clientId, Pageable pageable);
}
