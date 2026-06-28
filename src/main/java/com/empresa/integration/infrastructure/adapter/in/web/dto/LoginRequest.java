package com.empresa.integration.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Request de autenticacion con usuario y contrasena. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
