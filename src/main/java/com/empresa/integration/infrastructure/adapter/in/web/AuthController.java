package com.empresa.integration.infrastructure.adapter.in.web;

import com.empresa.integration.infrastructure.adapter.in.web.dto.LoginRequest;
import com.empresa.integration.infrastructure.adapter.in.web.dto.TokenResponse;
import com.empresa.integration.infrastructure.security.JwtTokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de autenticacion: login y renovacion de token. */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Autenticacion y gestion de tokens JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Construye el controller con sus dependencias.
     *
     * @param authenticationManager gestor de autenticacion de Spring Security
     * @param jwtTokenProvider proveedor de tokens JWT
     */
    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Autentica al usuario y retorna un token JWT.
     *
     * @param request credenciales del usuario
     * @return token JWT con tiempo de expiracion
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica usuario y retorna token JWT")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        String token = jwtTokenProvider.generateToken(auth.getName(), List.of("ROLE_USER"));
        return ResponseEntity.ok(new TokenResponse(token, jwtTokenProvider.getExpirationSeconds()));
    }
}
