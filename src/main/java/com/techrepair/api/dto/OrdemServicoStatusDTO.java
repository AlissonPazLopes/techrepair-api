package com.techrepair.api.dto;

import com.techrepair.api.entity.StatusOrdemServico;
import jakarta.validation.constraints.NotNull;

public record OrdemServicoStatusDTO(
        @NotNull(message = "O novo status é obrigatório")
        StatusOrdemServico status,

        String observacoes
) {
}
