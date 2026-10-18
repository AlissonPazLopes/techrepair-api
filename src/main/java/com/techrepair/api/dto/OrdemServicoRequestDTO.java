package com.techrepair.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrdemServicoRequestDTO(
        @NotNull(message = "O ID do cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "O ID do equipamento é obrigatório")
        Long equipamentoId,

        @NotBlank(message = "O defeito relatado é obrigatório")
        String defeitoRelatado,

        String observacoes
) {
}
