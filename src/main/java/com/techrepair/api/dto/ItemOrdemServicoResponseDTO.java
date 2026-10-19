package com.techrepair.api.dto;

import com.techrepair.api.entity.ItemOrdemServico;

import java.math.BigDecimal;

public record ItemOrdemServicoResponseDTO(
        Long id,
        Long ordemServicoId,
        Long pecaId,
        String pecaNome,
        String codigoSku,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static ItemOrdemServicoResponseDTO fromEntity(ItemOrdemServico item) {
        return new ItemOrdemServicoResponseDTO(
                item.getId(),
                item.getOrdemServico().getId(),
                item.getPeca().getId(),
                item.getPeca().getNome(),
                item.getPeca().getCodigoSku(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
        );
    }
}
