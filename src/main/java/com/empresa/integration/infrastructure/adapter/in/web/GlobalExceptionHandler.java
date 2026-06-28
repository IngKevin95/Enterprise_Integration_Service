package com.empresa.integration.infrastructure.adapter.in.web;

import com.empresa.integration.domain.exception.BusinessRuleException;
import com.empresa.integration.domain.exception.NotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Manejador global de excepciones — retorna Problem Details (RFC 7807). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja recursos no encontrados (404).
     *
     * @param ex excepcion de recurso no encontrado
     * @return problem detail con status 404
     */
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Not Found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /**
     * Maneja violaciones de reglas de negocio (422).
     *
     * @param ex excepcion de regla de negocio
     * @return problem detail con status 422
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("Business Rule Violation");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /**
     * Maneja errores de validacion de Bean Validation (400).
     *
     * @param ex excepcion de validacion
     * @return problem detail con status 400 y lista de violaciones
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setDetail("One or more fields are invalid");
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList());
        return problem;
    }
}
