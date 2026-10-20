package com.techrepair.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrepair.api.dto.AuthLoginRequestDTO;
import com.techrepair.api.dto.AuthRegistroRequestDTO;
import com.techrepair.api.entity.PerfilUsuario;
import com.techrepair.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve registrar usuário, realizar login recebendo JWT e acessar rota protegida com Bearer Token")
    void deveRegistrarLogarEAcessarRotaProtegidaComJwt() throws Exception {
        AuthRegistroRequestDTO registro = new AuthRegistroRequestDTO(
                "Administrador TechRepair",
                "admin@techrepair.com",
                "senhaSegura123",
                PerfilUsuario.ADMIN
        );

        // 1. Registra novo usuário
        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@techrepair.com"))
                .andExpect(jsonPath("$.perfil").value("ADMIN"));

        // 2. Realiza login e obtém o token JWT
        AuthLoginRequestDTO login = new AuthLoginRequestDTO("admin@techrepair.com", "senhaSegura123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.perfil").value("ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String jwtToken = objectMapper.readTree(loginResponse).get("token").asText();

        // 3. Tenta acessar rota protegida sem token -> deve negar (403 Forbidden)
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isForbidden());

        // 4. Acessa rota protegida enviando Authorization: Bearer <token> -> 200 OK
        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized padronizado quando credenciais de login forem inválidas")
    void deveRetornarUnauthorizedParaCredenciaisInvalidas() throws Exception {
        AuthLoginRequestDTO loginInvalido = new AuthLoginRequestDTO("inexistente@techrepair.com", "senhaErrada");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginInvalido)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro").isNotEmpty())
                .andExpect(jsonPath("$.mensagem").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler deve padronizar erros 400 (validação com camposInvalidos) e 404 (recurso não encontrado)")
    @WithMockUser(roles = "ADMIN")
    void deveRetornarJsonPadronizadoDoGlobalExceptionHandler() throws Exception {
        // 1. Erro de validação (400 Bad Request) com lista de camposInvalidos
        AuthRegistroRequestDTO registroInvalido = new AuthRegistroRequestDTO(
                "",
                "email-invalido",
                "123",
                null
        );

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/auth/registrar"))
                .andExpect(jsonPath("$.camposInvalidos").isArray())
                .andExpect(jsonPath("$.camposInvalidos").isNotEmpty());

        // 2. Recurso não encontrado (404 Not Found) padronizado
        mockMvc.perform(get("/api/clientes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/clientes/999999"));
    }
}
