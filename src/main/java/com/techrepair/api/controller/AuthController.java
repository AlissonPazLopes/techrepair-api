package com.techrepair.api.controller;

import com.techrepair.api.dto.AuthLoginRequestDTO;
import com.techrepair.api.dto.AuthRegistroRequestDTO;
import com.techrepair.api.dto.AuthTokenResponseDTO;
import com.techrepair.api.dto.UsuarioResponseDTO;
import com.techrepair.api.entity.Usuario;
import com.techrepair.api.repository.UsuarioRepository;
import com.techrepair.api.security.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO dto) {
        var authToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
        var authentication = authenticationManager.authenticate(authToken);

        Usuario usuario = (Usuario) authentication.getPrincipal();
        String jwtToken = tokenService.gerarToken(usuario);

        AuthTokenResponseDTO response = new AuthTokenResponseDTO(
                jwtToken,
                "Bearer",
                usuario.getEmail(),
                usuario.getNome(),
                usuario.getPerfil()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody AuthRegistroRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este e-mail");
        }

        Usuario novoUsuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(dto.perfil())
                .ativo(true)
                .build();

        Usuario salvo = usuarioRepository.save(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponseDTO.fromEntity(salvo));
    }
}
