package com.empresa.integration.infrastructure.adapter.in.web.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/** Respuesta paginada generica para endpoints REST. */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {

    /** Crea un PagedResponse a partir de un Page de Spring Data. */
    public static <T> PagedResponse<T> from(Page<T> source) {
        return new PagedResponse<>(
            source.getContent(),
            source.getNumber(),
            source.getSize(),
            source.getTotalElements(),
            source.getTotalPages(),
            source.isLast());
    }
}
