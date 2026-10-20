package com.techrepair.api.dto;

import com.techrepair.api.entity.PerfilUsuario;

public record AuthTokenResponseDTO(
        String token,
        String tipo,
        String email,
        String nome,
        PerfilUsuario perfil
) {
}
