package com.techrepair.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrepair.api.dto.ClienteRequestDTO;
import com.techrepair.api.dto.EquipamentoRequestDTO;
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
class EquipamentoControllerTest {

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

    private Long criarClientePadrao() throws Exception {
        ClienteRequestDTO request = new ClienteRequestDTO(
                "Ana Pereira",
                "999.888.777-66",
                "(31) 98888-0000",
                "ana@email.com"
        );

        String responseJson = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseJson).get("id").asLong();
    }

    @Test
    @DisplayName("Deve cadastrar um equipamento vinculado a um cliente e retornar 201 Created")
    void deveCadastrarEquipamentoComSucesso() throws Exception {
        Long clienteId = criarClientePadrao();

        EquipamentoRequestDTO request = new EquipamentoRequestDTO(
                clienteId,
                "Notebook",
                "Dell",
                "Inspiron 15",
                "SN-DELL-12345",
                "Não liga, LED piscando"
        );

        mockMvc.perform(post("/api/equipamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.clienteId").value(clienteId))
                .andExpect(jsonPath("$.clienteNome").value("Ana Pereira"))
                .andExpect(jsonPath("$.tipo").value("Notebook"))
                .andExpect(jsonPath("$.marca").value("Dell"))
                .andExpect(jsonPath("$.modelo").value("Inspiron 15"))
                .andExpect(jsonPath("$.numeroSerie").value("SN-DELL-12345"));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar cadastrar equipamento para cliente inexistente")
    void deveRetornarNotFoundParaClienteInexistente() throws Exception {
        EquipamentoRequestDTO request = new EquipamentoRequestDTO(
                99999L,
                "Smartphone",
                "Samsung",
                "Galaxy S23",
                "SN-SAM-999",
                "Tela trincada"
        );

        mockMvc.perform(post("/api/equipamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar equipamentos por cliente e buscar equipamento por ID")
    void deveListarEBuscarEquipamentoPorId() throws Exception {
        Long clienteId = criarClientePadrao();

        EquipamentoRequestDTO request = new EquipamentoRequestDTO(
                clienteId,
                "Console",
                "Sony",
                "PlayStation 5",
                "SN-PS5-001",
                "Superaquecimento"
        );

        String responseJson = mockMvc.perform(post("/api/equipamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long equipamentoId = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(get("/api/equipamentos").param("clienteId", clienteId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].modelo").value("PlayStation 5"));

        mockMvc.perform(get("/api/equipamentos/{id}", equipamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(equipamentoId))
                .andExpect(jsonPath("$.marca").value("Sony"));

        mockMvc.perform(get("/api/equipamentos/{id}", 99999L))
                .andExpect(status().isNotFound());
    }
}
