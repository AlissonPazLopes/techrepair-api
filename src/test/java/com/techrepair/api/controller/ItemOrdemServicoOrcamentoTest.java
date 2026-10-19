package com.techrepair.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrepair.api.dto.ClienteRequestDTO;
import com.techrepair.api.dto.EquipamentoRequestDTO;
import com.techrepair.api.dto.ItemOrdemServicoRequestDTO;
import com.techrepair.api.dto.MovimentacaoEstoqueDTO;
import com.techrepair.api.dto.OrdemServicoDiagnosticoDTO;
import com.techrepair.api.dto.OrdemServicoRequestDTO;
import com.techrepair.api.dto.OrdemServicoStatusDTO;
import com.techrepair.api.dto.PecaRequestDTO;
import com.techrepair.api.entity.StatusOrdemServico;
import com.techrepair.api.repository.ClienteRepository;
import com.techrepair.api.repository.EquipamentoRepository;
import com.techrepair.api.repository.ItemOrdemServicoRepository;
import com.techrepair.api.repository.OrdemServicoRepository;
import com.techrepair.api.repository.PecaRepository;
import org.junit.jupiter.api.AfterEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemOrdemServicoOrcamentoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemOrdemServicoRepository itemOrdemServicoRepository;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PecaRepository pecaRepository;

    @BeforeEach
    void setUp() {
        limparBase();
    }

    @AfterEach
    void tearDown() {
        limparBase();
    }

    private void limparBase() {
        itemOrdemServicoRepository.deleteAll();
        ordemServicoRepository.deleteAll();
        equipamentoRepository.deleteAll();
        clienteRepository.deleteAll();
        pecaRepository.deleteAll();
    }

    private Long criarOsPadrao() throws Exception {
        ClienteRequestDTO clienteReq = new ClienteRequestDTO(
                "Lucas Martins", "444.555.666-77", "(11) 97777-8888", "lucas@email.com"
        );
        String clienteJson = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long clienteId = objectMapper.readTree(clienteJson).get("id").asLong();

        EquipamentoRequestDTO eqReq = new EquipamentoRequestDTO(
                clienteId, "Notebook", "Asus", "VivoBook 15", "SN-ASUS-77", null
        );
        String eqJson = mockMvc.perform(post("/api/equipamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eqReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long equipamentoId = objectMapper.readTree(eqJson).get("id").asLong();

        OrdemServicoRequestDTO osReq = new OrdemServicoRequestDTO(
                clienteId, equipamentoId, "Lentidão extrema e bateria viciada", null
        );
        String osJson = mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(osReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(osJson).get("id").asLong();
    }

    private Long criarPeca(String nome, String sku, String precoVenda, int estoque) throws Exception {
        PecaRequestDTO pecaReq = new PecaRequestDTO(
                nome,
                sku,
                "Peça original",
                new BigDecimal("100.00"),
                new BigDecimal(precoVenda),
                estoque,
                2
        );
        String pecaJson = mockMvc.perform(post("/api/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pecaReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(pecaJson).get("id").asLong();
    }

    @Test
    @DisplayName("Deve calcular automaticamente valorPecas e valorTotal ao adicionar e remover itens da OS")
    void deveCalcularOrcamentoAutomaticamente() throws Exception {
        Long osId = criarOsPadrao();
        Long ssdId = criarPeca("SSD 480GB", "SSD-480", "250.00", 10);
        Long memoriaId = criarPeca("RAM 8GB DDR4", "RAM-8G", "150.00", 10);

        // Avança OS para EM_ANALISE e define mão de obra = 120.00
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.EM_ANALISE, null))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/ordens-servico/{id}/diagnostico", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoDiagnosticoDTO("Upgrade SSD + RAM", new BigDecimal("120.00"), null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorMaoDeObra").value(120.00))
                .andExpect(jsonPath("$.valorTotal").value(120.00));

        // Adiciona 1x SSD (250.00)
        String item1Json = mockMvc.perform(post("/api/ordens-servico/{osId}/itens", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemOrdemServicoRequestDTO(ssdId, 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precoUnitario").value(250.00))
                .andExpect(jsonPath("$.subtotal").value(250.00))
                .andReturn().getResponse().getContentAsString();
        Long item1Id = objectMapper.readTree(item1Json).get("id").asLong();

        // Adiciona 2x RAM 8GB (2 * 150.00 = 300.00)
        mockMvc.perform(post("/api/ordens-servico/{osId}/itens", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemOrdemServicoRequestDTO(memoriaId, 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(300.00));

        // Verifica cálculo: valorPecas = 550.00, valorMaoDeObra = 120.00 -> valorTotal = 670.00
        mockMvc.perform(get("/api/ordens-servico/{id}", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorPecas").value(550.00))
                .andExpect(jsonPath("$.valorMaoDeObra").value(120.00))
                .andExpect(jsonPath("$.valorTotal").value(670.00));

        // Remove o item 1 (SSD 250.00) -> valorPecas = 300.00, valorTotal = 420.00
        mockMvc.perform(delete("/api/ordens-servico/{osId}/itens/{itemId}", osId, item1Id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/ordens-servico/{id}", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorPecas").value(300.00))
                .andExpect(jsonPath("$.valorTotal").value(420.00));
    }

    @Test
    @DisplayName("Deve dar baixa automática no estoque ao aprovar OS (AGUARDANDO_APROVACAO -> EM_CONSERTO) e bloquear novas adições")
    void deveBaixarEstoqueNaAprovacaoEBloquearAlteracoesEmConserto() throws Exception {
        Long osId = criarOsPadrao();
        Long bateriaId = criarPeca("Bateria Asus", "BAT-ASUS-01", "220.00", 5);

        // Adiciona 2 unidades da bateria na OS (estoque permanece 5 enquanto não aprovar)
        String itemJson = mockMvc.perform(post("/api/ordens-servico/{osId}/itens", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemOrdemServicoRequestDTO(bateriaId, 2))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long itemId = objectMapper.readTree(itemJson).get("id").asLong();

        mockMvc.perform(get("/api/pecas/{id}", bateriaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(5));

        // Avança ABERTA -> EM_ANALISE -> AGUARDANDO_APROVACAO
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.EM_ANALISE, null))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.AGUARDANDO_APROVACAO, null))))
                .andExpect(status().isOk());

        // Aprova orçamento: AGUARDANDO_APROVACAO -> EM_CONSERTO (deve baixar 2 unidades do estoque: 5 - 2 = 3)
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.EM_CONSERTO, "Cliente aprovou"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_CONSERTO"));

        // Confirma que o estoque da peça caiu automaticamente de 5 para 3
        mockMvc.perform(get("/api/pecas/{id}", bateriaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(3));

        // Deve bloquear adição de nova peça quando já está EM_CONSERTO
        mockMvc.perform(post("/api/ordens-servico/{osId}/itens", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemOrdemServicoRequestDTO(bateriaId, 1))))
                .andExpect(status().isBadRequest());

        // Deve bloquear remoção de peça quando já está EM_CONSERTO
        mockMvc.perform(delete("/api/ordens-servico/{osId}/itens/{itemId}", osId, itemId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve desfazer transação (rollback) na aprovação se faltar estoque no momento de iniciar conserto")
    void deveDesfazerTransacaoSeFaltarEstoqueNaAprovacao() throws Exception {
        Long osId = criarOsPadrao();
        Long telaId = criarPeca("Tela Full HD 15.6", "TELA-ASUS-15", "450.00", 2);

        // Adiciona 2 telas na OS
        mockMvc.perform(post("/api/ordens-servico/{osId}/itens", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemOrdemServicoRequestDTO(telaId, 2))))
                .andExpect(status().isCreated());

        // Avança para AGUARDANDO_APROVACAO
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.EM_ANALISE, null))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.AGUARDANDO_APROVACAO, null))))
                .andExpect(status().isOk());

        // Enquanto aguardava aprovação, 1 unidade da tela foi baixada manualmente no balcão (restou apenas 1)
        mockMvc.perform(patch("/api/pecas/{id}/estoque/baixa", telaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MovimentacaoEstoqueDTO(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(1));

        // Tenta aprovar a OS que exige 2 telas -> deve falhar com 400 Bad Request e manter OS em AGUARDANDO_APROVACAO
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.EM_CONSERTO, null))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/ordens-servico/{id}", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"));

        mockMvc.perform(get("/api/ordens-servico/{osId}/itens", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
