package com.techrepair.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovimentacaoEstoqueDTO(
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade de movimentação deve ser maior que zero")
        Integer quantidade
) {
}
