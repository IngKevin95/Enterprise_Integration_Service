package com.empresa.integration.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Genera y valida tokens JWT para la autenticacion de la API. */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    /**
     * Construye el proveedor de tokens con el secreto y TTL configurados.
     *
     * @param secret clave secreta minimo 256 bits
     * @param expirationMs tiempo de vida del token en milisegundos
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Genera un token JWT para el usuario y roles indicados.
     *
     * @param username nombre de usuario (subject del token)
     * @param roles lista de roles del usuario
     * @return token JWT firmado
     */
    public String generateToken(String username, List<String> roles) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(new Date(now))
            .expiration(new Date(now + expirationMs))
            .signWith(secretKey)
            .compact();
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     *
     * @param token token JWT a parsear
     * @return username contenido en el token
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Valida que el token JWT sea valido y no este expirado.
     *
     * @param token token a validar
     * @return true si el token es valido
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Retorna el tiempo de vida del token en segundos.
     *
     * @return tiempo de expiracion en segundos
     */
    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
