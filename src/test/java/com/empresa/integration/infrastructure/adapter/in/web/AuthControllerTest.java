package com.empresa.integration.infrastructure.adapter.in.web;

import com.empresa.integration.infrastructure.adapter.in.web.dto.TokenResponse;
import com.empresa.integration.infrastructure.security.JwtTokenProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void login_withValidCredentials_returns200AndToken() throws Exception {
        UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken(
            "admin",
            null,
            User.withUsername("admin").password("x").roles("USER").build().getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authResult);
        when(jwtTokenProvider.generateToken("admin", List.of("ROLE_USER"))).thenReturn("test-jwt-token");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(3600L);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Object() {
                    public String username = "admin";
                    public String password = "admin123";
                })))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("test-jwt-token"))
            .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void login_withBlankUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"admin123\"}"))
            .andExpect(status().isBadRequest());
    }
}
