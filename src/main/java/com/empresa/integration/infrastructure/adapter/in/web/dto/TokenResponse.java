package com.empresa.integration.infrastructure.adapter.in.web.dto;

/** Respuesta de autenticacion con el token JWT y su tiempo de expiracion. */
public record TokenResponse(String token, long expiresIn) {
}
