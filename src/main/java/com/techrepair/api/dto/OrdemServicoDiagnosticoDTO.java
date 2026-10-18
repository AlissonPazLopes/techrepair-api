package com.techrepair.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record OrdemServicoDiagnosticoDTO(
        @NotBlank(message = "O diagnóstico técnico é obrigatório")
        String diagnosticoTecnico,

        @NotNull(message = "O valor da mão de obra é obrigatório")
        @PositiveOrZero(message = "O valor da mão de obra não pode ser negativo")
        BigDecimal valorMaoDeObra,

        String observacoes
) {
}
