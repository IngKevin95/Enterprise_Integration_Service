package com.empresa.integration.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.springframework.context.annotation.Configuration;

/** Configuracion de la documentacion OpenAPI con esquema de seguridad Bearer. */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Enterprise Integration Service API",
        version = "v1",
        description = "API REST para integracion empresarial con sistemas legacy"
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
}
