package com.techrepair.api.dto;

import com.techrepair.api.entity.Equipamento;
import com.techrepair.api.entity.OrdemServico;
import com.techrepair.api.entity.StatusOrdemServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemServicoResponseDTO(
        Long id,
        String numeroOs,
        Long clienteId,
        String clienteNome,
        Long equipamentoId,
        String equipamentoDescricao,
        String defeitoRelatado,
        String diagnosticoTecnico,
        StatusOrdemServico status,
        BigDecimal valorMaoDeObra,
        BigDecimal valorPecas,
        BigDecimal valorTotal,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        String observacoes
) {
    public static OrdemServicoResponseDTO fromEntity(OrdemServico os) {
        Equipamento eq = os.getEquipamento();
        String descricaoEquipamento = String.format("%s %s %s", eq.getTipo(), eq.getMarca(), eq.getModelo());

        return new OrdemServicoResponseDTO(
                os.getId(),
                os.getNumeroOs(),
                os.getCliente().getId(),
                os.getCliente().getNome(),
                eq.getId(),
                descricaoEquipamento,
                os.getDefeitoRelatado(),
                os.getDiagnosticoTecnico(),
                os.getStatus(),
                os.getValorMaoDeObra(),
                os.getValorPecas(),
                os.getValorTotal(),
                os.getDataAbertura(),
                os.getDataFechamento(),
                os.getObservacoes()
        );
    }
}
