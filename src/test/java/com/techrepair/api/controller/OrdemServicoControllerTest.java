package com.techrepair.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techrepair.api.dto.ClienteRequestDTO;
import com.techrepair.api.dto.EquipamentoRequestDTO;
import com.techrepair.api.dto.OrdemServicoDiagnosticoDTO;
import com.techrepair.api.dto.OrdemServicoRequestDTO;
import com.techrepair.api.dto.OrdemServicoStatusDTO;
import com.techrepair.api.entity.StatusOrdemServico;
import com.techrepair.api.repository.ClienteRepository;
import com.techrepair.api.repository.EquipamentoRepository;
import com.techrepair.api.repository.OrdemServicoRepository;
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
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.security.test.context.support.WithMockUser(roles = "TECNICO")
class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setUp() {
        limparBase();
    }

    @AfterEach
    void tearDown() {
        limparBase();
    }

    private void limparBase() {
        ordemServicoRepository.deleteAll();
        equipamentoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    private Long criarCliente(String nome, String doc, String email) throws Exception {
        ClienteRequestDTO request = new ClienteRequestDTO(nome, doc, "(11) 99999-0000", email);
        String json = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    private Long criarEquipamento(Long clienteId, String modelo) throws Exception {
        EquipamentoRequestDTO request = new EquipamentoRequestDTO(
                clienteId, "Notebook", "Lenovo", modelo, "SN-LEN-001", "Sem arranhões"
        );
        String json = mockMvc.perform(post("/api/equipamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    @Test
    @DisplayName("Deve abrir uma Ordem de Serviço com status ABERTA, número gerado e valores zerados")
    void deveAbrirOrdemServicoComSucesso() throws Exception {
        Long clienteId = criarCliente("Roberto Almeida", "111.222.333-01", "roberto@email.com");
        Long equipamentoId = criarEquipamento(clienteId, "ThinkPad T14");

        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                clienteId,
                equipamentoId,
                "Teclado falhando algumas teclas",
                "Deixou carregador junto"
        );

        mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.numeroOs", startsWith("OS-")))
                .andExpect(jsonPath("$.clienteId").value(clienteId))
                .andExpect(jsonPath("$.equipamentoId").value(equipamentoId))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.valorMaoDeObra").value(0))
                .andExpect(jsonPath("$.valorPecas").value(0))
                .andExpect(jsonPath("$.valorTotal").value(0))
                .andExpect(jsonPath("$.dataAbertura").isNotEmpty())
                .andExpect(jsonPath("$.dataFechamento").isEmpty());
    }

    @Test
    @DisplayName("Deve bloquear abertura de OS (400 Bad Request) quando equipamento não pertence ao cliente")
    void deveBloquearEquipamentoDeOutroCliente() throws Exception {
        Long cliente1 = criarCliente("Cliente Um", "111.111.111-01", "c1@email.com");
        Long cliente2 = criarCliente("Cliente Dois", "222.222.222-02", "c2@email.com");
        Long equipamentoDoCliente2 = criarEquipamento(cliente2, "IdeaPad 3");

        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                cliente1,
                equipamentoDoCliente2,
                "Não dá vídeo",
                null
        );

        mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve percorrer todo o fluxo válido da máquina de estados até FINALIZADA e bloquear novas edições")
    void deveExecutarFluxoCompletoMaquinaDeEstados() throws Exception {
        Long clienteId = criarCliente("Fernanda Lima", "333.444.555-03", "fernanda@email.com");
        Long equipamentoId = criarEquipamento(clienteId, "Legion 5i");

        OrdemServicoRequestDTO abertura = new OrdemServicoRequestDTO(
                clienteId, equipamentoId, "Superaquecimento e desligamento", null
        );

        String responseJson = mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abertura)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long osId = objectMapper.readTree(responseJson).get("id").asLong();

        // 1. Transição ABERTA -> EM_ANALISE
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.EM_ANALISE, "Iniciando análise"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));

        // 2. Atualiza Diagnóstico Técnico e Mão de Obra
        OrdemServicoDiagnosticoDTO diagnostico = new OrdemServicoDiagnosticoDTO(
                "Necessária limpeza completa do cooler e troca de pasta térmica",
                new BigDecimal("180.00"),
                "Orçamento enviado ao cliente"
        );
        mockMvc.perform(patch("/api/ordens-servico/{id}/diagnostico", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diagnostico)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosticoTecnico").value("Necessária limpeza completa do cooler e troca de pasta térmica"))
                .andExpect(jsonPath("$.valorMaoDeObra").value(180.00))
                .andExpect(jsonPath("$.valorTotal").value(180.00));

        // 3. Transição EM_ANALISE -> AGUARDANDO_APROVACAO
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.AGUARDANDO_APROVACAO, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"));

        // 4. Transição AGUARDANDO_APROVACAO -> EM_CONSERTO
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.EM_CONSERTO, "Aprovado pelo cliente"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_CONSERTO"));

        // 5. Transição EM_CONSERTO -> FINALIZADA (deve preencher dataFechamento)
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.FINALIZADA, "Conserto concluído"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"))
                .andExpect(jsonPath("$.dataFechamento").isNotEmpty());

        // 6. Deve bloquear nova transição após FINALIZADA
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.CANCELADA, "Tentativa inválida"))))
                .andExpect(status().isBadRequest());

        // 7. Deve bloquear edição de diagnóstico após FINALIZADA
        mockMvc.perform(patch("/api/ordens-servico/{id}/diagnostico", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diagnostico)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve bloquear transições inválidas na máquina de estados (ex: ABERTA direto para FINALIZADA)")
    void deveBloquearTransicaoDeStatusInvalida() throws Exception {
        Long clienteId = criarCliente("Marcos Paulo", "777.888.999-04", "marcos@email.com");
        Long equipamentoId = criarEquipamento(clienteId, "Yoga Slim 7");

        OrdemServicoRequestDTO abertura = new OrdemServicoRequestDTO(
                clienteId, equipamentoId, "Bateria não carrega", null
        );

        String responseJson = mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abertura)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long osId = objectMapper.readTree(responseJson).get("id").asLong();

        // Tentativa inválida: ABERTA -> FINALIZADA
        mockMvc.perform(patch("/api/ordens-servico/{id}/status", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrdemServicoStatusDTO(StatusOrdemServico.FINALIZADA, null))))
                .andExpect(status().isBadRequest());

        // Listagem filtrando por status ABERTA
        mockMvc.perform(get("/api/ordens-servico").param("status", "ABERTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
