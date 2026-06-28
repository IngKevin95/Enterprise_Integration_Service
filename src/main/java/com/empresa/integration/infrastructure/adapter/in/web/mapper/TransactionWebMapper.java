package com.empresa.integration.infrastructure.adapter.in.web.mapper;

import com.empresa.integration.domain.model.Transaction;
import com.empresa.integration.infrastructure.adapter.in.web.dto.TransactionResponse;

import org.mapstruct.Mapper;

/** Convierte Transaction de dominio a DTO de respuesta web. */
@Mapper(componentModel = "spring")
public interface TransactionWebMapper {

    TransactionResponse toResponse(Transaction transaction);
}
