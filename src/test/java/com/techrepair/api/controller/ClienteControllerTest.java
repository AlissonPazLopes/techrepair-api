package com.techrepair.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrepair.api.dto.ClienteRequestDTO;
import com.techrepair.api.repository.ClienteRepository;
import com.techrepair.api.repository.EquipamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.security.test.context.support.WithMockUser(roles = "ATENDENTE")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @BeforeEach
    void setUp() {
        equipamentoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve cadastrar um novo cliente com sucesso e retornar 201 Created")
    void deveCadastrarClienteComSucesso() throws Exception {
        ClienteRequestDTO request = new ClienteRequestDTO(
                "João da Silva",
                "123.456.789-00",
                "(11) 99999-8888",
                "joao@email.com"
        );

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("João da Silva"))
                .andExpect(jsonPath("$.documento").value("123.456.789-00"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.dataCadastro").isNotEmpty());
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao tentar cadastrar cliente com documento duplicado")
    void deveRejeitarDocumentoDuplicado() throws Exception {
        ClienteRequestDTO request1 = new ClienteRequestDTO(
                "Maria Oliveira",
                "111.222.333-44",
                "(11) 98888-7777",
                "maria@email.com"
        );
        ClienteRequestDTO request2 = new ClienteRequestDTO(
                "Maria Outra",
                "111.222.333-44",
                "(11) 97777-6666",
                "outra@email.com"
        );

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando dados obrigatórios forem inválidos")
    void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
        ClienteRequestDTO requestInvalido = new ClienteRequestDTO(
                "",
                "",
                "",
                "email-invalido"
        );

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve listar todos os clientes e buscar cliente por ID")
    void deveListarEBuscarPorId() throws Exception {
        ClienteRequestDTO request = new ClienteRequestDTO(
                "Carlos Souza",
                "555.666.777-88",
                "(21) 99999-1111",
                "carlos@email.com"
        );

        String responseJson = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long idCriado = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Carlos Souza"));

        mockMvc.perform(get("/api/clientes/{id}", idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idCriado))
                .andExpect(jsonPath("$.email").value("carlos@email.com"));

        mockMvc.perform(get("/api/clientes/{id}", 9999L))
                .andExpect(status().isNotFound());
    }
}
