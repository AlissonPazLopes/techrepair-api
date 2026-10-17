package com.techrepair.api.dto;

import com.techrepair.api.entity.Peca;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PecaResponseDTO(
        Long id,
        String nome,
        String codigoSku,
        String descricao,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        Integer quantidadeEstoque,
        Integer estoqueMinimo,
        Boolean estoqueBaixo,
        Boolean ativo,
        LocalDateTime dataCadastro
) {
    public static PecaResponseDTO fromEntity(Peca peca) {
        boolean emAlerta = peca.getQuantidadeEstoque() != null
                && peca.getEstoqueMinimo() != null
                && peca.getQuantidadeEstoque() <= peca.getEstoqueMinimo();

        return new PecaResponseDTO(
                peca.getId(),
                peca.getNome(),
                peca.getCodigoSku(),
                peca.getDescricao(),
                peca.getPrecoCusto(),
                peca.getPrecoVenda(),
                peca.getQuantidadeEstoque(),
                peca.getEstoqueMinimo(),
                emAlerta,
                peca.getAtivo(),
                peca.getDataCadastro()
        );
    }
}
