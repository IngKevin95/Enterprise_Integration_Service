package com.empresa.integration.application.service;

import com.empresa.integration.application.port.in.ClientQueryUseCase;
import com.empresa.integration.application.port.out.ClientRepository;
import com.empresa.integration.domain.exception.NotFoundException;
import com.empresa.integration.domain.model.Client;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Servicio de aplicacion para consultas de clientes. */
@Service
@Transactional(readOnly = true)
public class ClientService implements ClientQueryUseCase {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client findById(UUID id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Client", id));
    }
}
