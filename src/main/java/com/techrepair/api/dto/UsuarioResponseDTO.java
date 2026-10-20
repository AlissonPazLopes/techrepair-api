package com.techrepair.api.dto;

import com.techrepair.api.entity.PerfilUsuario;
import com.techrepair.api.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Boolean ativo,
        LocalDateTime dataCadastro
) {
    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getAtivo(),
                usuario.getDataCadastro()
        );
    }
}
