package com.techrepair.api.dto;

import com.techrepair.api.entity.Equipamento;

public record EquipamentoResponseDTO(
        Long id,
        Long clienteId,
        String clienteNome,
        String tipo,
        String marca,
        String modelo,
        String numeroSerie,
        String observacoes
) {
    public static EquipamentoResponseDTO fromEntity(Equipamento equipamento) {
        return new EquipamentoResponseDTO(
                equipamento.getId(),
                equipamento.getCliente().getId(),
                equipamento.getCliente().getNome(),
                equipamento.getTipo(),
                equipamento.getMarca(),
                equipamento.getModelo(),
                equipamento.getNumeroSerie(),
                equipamento.getObservacoes()
        );
    }
}
