package com.techrepair.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipamentoRequestDTO(
        @NotNull(message = "O ID do cliente é obrigatório")
        Long clienteId,

        @NotBlank(message = "O tipo do equipamento é obrigatório")
        @Size(max = 50, message = "O tipo deve ter no máximo 50 caracteres")
        String tipo,

        @NotBlank(message = "A marca é obrigatória")
        @Size(max = 100, message = "A marca deve ter no máximo 100 caracteres")
        String marca,

        @NotBlank(message = "O modelo é obrigatório")
        @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres")
        String modelo,

        @Size(max = 100, message = "O número de série deve ter no máximo 100 caracteres")
        String numeroSerie,

        String observacoes
) {
}
