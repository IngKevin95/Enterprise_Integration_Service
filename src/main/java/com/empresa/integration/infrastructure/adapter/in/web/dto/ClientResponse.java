package com.empresa.integration.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/** DTO de respuesta para cliente. */
public record ClientResponse(
        UUID id,
        String name,
        String documentNumber,
        String email,
        String legacyId) { }
