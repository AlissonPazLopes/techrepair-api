package com.techrepair.api.dto;

import com.techrepair.api.entity.Cliente;

import java.time.LocalDateTime;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String documento,
        String telefone,
        String email,
        LocalDateTime dataCadastro
) {
    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getDataCadastro()
        );
    }
}
