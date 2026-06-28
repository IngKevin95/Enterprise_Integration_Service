package com.empresa.integration.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET =
        "test-secret-at-least-256-bits-long-for-hmac-sha256-key-ok";
    private static final long EXPIRATION_MS = 3600_000L;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = provider.generateToken("alice", List.of("ROLE_USER"));
        assertThat(token).isNotBlank();
    }

    @Test
    void getUsername_returnsCorrectSubject() {
        String token = provider.generateToken("alice", List.of("ROLE_USER"));
        assertThat(provider.getUsername(token)).isEqualTo("alice");
    }

    @Test
    void isValid_returnsTrueForFreshToken() {
        String token = provider.generateToken("alice", List.of("ROLE_USER"));
        assertThat(provider.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalseForGarbage() {
        assertThat(provider.isValid("not.a.jwt")).isFalse();
    }

    @Test
    void isValid_returnsFalseForTokenSignedWithWrongKey() {
        JwtTokenProvider other = new JwtTokenProvider(
            "different-secret-also-at-least-256-bits-long-hmac-sha256-ok", EXPIRATION_MS);
        String tokenFromOther = other.generateToken("bob", List.of("ROLE_USER"));
        assertThat(provider.isValid(tokenFromOther)).isFalse();
    }

    @Test
    void getExpirationSeconds_returnsMillisConvertedToSeconds() {
        assertThat(provider.getExpirationSeconds()).isEqualTo(EXPIRATION_MS / 1000);
    }
}
