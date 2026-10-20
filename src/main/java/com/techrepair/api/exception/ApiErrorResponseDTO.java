package com.techrepair.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponseDTO(
        Instant timestamp,
        Integer status,
        String erro,
        String mensagem,
        String path,
        List<CampoErroDTO> camposInvalidos
) {
    public record CampoErroDTO(String campo, String mensagem) {
    }
}
