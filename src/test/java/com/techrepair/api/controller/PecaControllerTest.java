package com.techrepair.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrepair.api.dto.MovimentacaoEstoqueDTO;
import com.techrepair.api.dto.PecaRequestDTO;
import com.techrepair.api.repository.PecaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PecaRepository pecaRepository;

    @BeforeEach
    void setUp() {
        pecaRepository.deleteAll();
    }

    private Long criarPecaPadrao(String sku, int qtdEstoque, int estoqueMinimo) throws Exception {
        PecaRequestDTO request = new PecaRequestDTO(
                "SSD NVMe 512GB",
                sku,
                "SSD M.2 PCIe Gen4",
                new BigDecimal("180.00"),
                new BigDecimal("320.00"),
                qtdEstoque,
                estoqueMinimo
        );

        String responseJson = mockMvc.perform(post("/api/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseJson).get("id").asLong();
    }

    @Test
    @DisplayName("Deve cadastrar uma nova peça com sucesso e retornar 201 Created")
    void deveCadastrarPecaComSucesso() throws Exception {
        PecaRequestDTO request = new PecaRequestDTO(
                "Memória RAM 16GB DDR4",
                "MEM-DDR4-16G",
                "Módulo 3200MHz Notebook",
                new BigDecimal("150.00"),
                new BigDecimal("280.00"),
                10,
                3
        );

        mockMvc.perform(post("/api/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Memória RAM 16GB DDR4"))
                .andExpect(jsonPath("$.codigoSku").value("MEM-DDR4-16G"))
                .andExpect(jsonPath("$.quantidadeEstoque").value(10))
                .andExpect(jsonPath("$.estoqueMinimo").value(3))
                .andExpect(jsonPath("$.estoqueBaixo").value(false))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao tentar cadastrar peça com SKU duplicado")
    void deveRejeitarSkuDuplicado() throws Exception {
        criarPecaPadrao("SKU-UNICO-001", 10, 5);

        PecaRequestDTO duplicada = new PecaRequestDTO(
                "Outra Peça",
                "SKU-UNICO-001",
                "Descrição",
                new BigDecimal("50.00"),
                new BigDecimal("100.00"),
                5,
                2
        );

        mockMvc.perform(post("/api/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicada)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve realizar entrada e baixa de estoque quando houver saldo suficiente")
    void deveMovimentarEstoqueComSucesso() throws Exception {
        Long pecaId = criarPecaPadrao("SSD-512-001", 10, 5);

        // Entrada de +5 unidades (total esperado: 15)
        MovimentacaoEstoqueDTO entrada = new MovimentacaoEstoqueDTO(5);
        mockMvc.perform(patch("/api/pecas/{id}/estoque/entrada", pecaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(15));

        // Baixa de -6 unidades (total esperado: 9)
        MovimentacaoEstoqueDTO baixa = new MovimentacaoEstoqueDTO(6);
        mockMvc.perform(patch("/api/pecas/{id}/estoque/baixa", pecaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baixa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(9));
    }

    @Test
    @DisplayName("Deve bloquear baixa de estoque (400 Bad Request) quando saldo for insuficiente")
    void deveBloquearBaixaComEstoqueInsuficiente() throws Exception {
        Long pecaId = criarPecaPadrao("BATERIA-DELL-01", 4, 5);

        MovimentacaoEstoqueDTO baixaExcessiva = new MovimentacaoEstoqueDTO(5);

        mockMvc.perform(patch("/api/pecas/{id}/estoque/baixa", pecaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baixaExcessiva)))
                .andExpect(status().isBadRequest());

        // Confirma que o estoque permaneceu intacto em 4 unidades
        mockMvc.perform(get("/api/pecas/{id}", pecaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(4));
    }

    @Test
    @DisplayName("Deve listar apenas peças ativas com estoque igual ou abaixo do mínimo")
    void deveListarPecasComEstoqueBaixo() throws Exception {
        criarPecaPadrao("TELA-156-FHD", 3, 5);   // Em alerta (3 <= 5)
        criarPecaPadrao("PASTA-TERMICA", 5, 5);  // Em alerta (5 <= 5)
        criarPecaPadrao("CABO-HDMI-2M", 20, 5);  // Estoque normal (20 > 5)

        mockMvc.perform(get("/api/pecas/estoque-baixo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
