package com.techrepair.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemOrdemServicoRequestDTO(
        @NotNull(message = "O ID da peça é obrigatório")
        Long pecaId,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade
) {
}
