package com.empresa.integration.infrastructure.adapter.in.web;

import com.empresa.integration.domain.exception.BusinessRuleException;
import com.empresa.integration.domain.exception.NotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404ProblemDetail() {
        NotFoundException ex = new NotFoundException("Transaction", UUID.randomUUID());
        ProblemDetail result = handler.handleNotFound(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("Not Found");
    }

    @Test
    void handleBusinessRule_returns422ProblemDetail() {
        BusinessRuleException ex = new BusinessRuleException("importe negativo no permitido");
        ProblemDetail result = handler.handleBusinessRule(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(result.getTitle()).isEqualTo("Business Rule Violation");
        assertThat(result.getDetail()).contains("importe negativo");
    }

    @Test
    void handleBusinessRule_viaMockMvc_returns422() throws Exception {
        @RestController
        class StubController {
            @GetMapping("/test-business")
            String trigger() {
                throw new BusinessRuleException("regla violada");
            }
        }

        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new StubController())
            .setControllerAdvice(handler)
            .build();

        mockMvc.perform(get("/test-business"))
            .andExpect(status().isUnprocessableEntity());
    }
}
