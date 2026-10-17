package com.techrepair.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PecaRequestDTO(
        @NotBlank(message = "O nome da peça é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "O código SKU é obrigatório")
        @Size(max = 50, message = "O código SKU deve ter no máximo 50 caracteres")
        String codigoSku,

        String descricao,

        @NotNull(message = "O preço de custo é obrigatório")
        @Positive(message = "O preço de custo deve ser maior que zero")
        BigDecimal precoCusto,

        @NotNull(message = "O preço de venda é obrigatório")
        @Positive(message = "O preço de venda deve ser maior que zero")
        BigDecimal precoVenda,

        @PositiveOrZero(message = "A quantidade em estoque não pode ser negativa")
        Integer quantidadeEstoque,

        @PositiveOrZero(message = "O estoque mínimo não pode ser negativo")
        Integer estoqueMinimo
) {
}
