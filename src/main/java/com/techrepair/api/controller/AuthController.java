package com.techrepair.api.controller;

import com.techrepair.api.dto.AuthLoginRequestDTO;
import com.techrepair.api.dto.AuthRegistroRequestDTO;
import com.techrepair.api.dto.AuthTokenResponseDTO;
import com.techrepair.api.dto.UsuarioResponseDTO;
import com.techrepair.api.entity.Usuario;
import com.techrepair.api.repository.UsuarioRepository;
import com.techrepair.api.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticação", description = "Endpoints públicos para login e registro de usuários com JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário e gerar token JWT",
            description = "Valida as credenciais de e-mail e senha do usuário e retorna um token Bearer JWT válido por 2 horas."
    )
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
    @Operation(
            summary = "Registrar novo usuário",
            description = "Cadastra um novo usuário no sistema (ADMIN, TECNICO ou ATENDENTE) com senha criptografada via BCrypt."
    )
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
