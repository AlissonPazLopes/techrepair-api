package com.techrepair.api.controller;

import com.techrepair.api.dto.ItemOrdemServicoRequestDTO;
import com.techrepair.api.dto.ItemOrdemServicoResponseDTO;
import com.techrepair.api.dto.OrdemServicoDiagnosticoDTO;
import com.techrepair.api.dto.OrdemServicoRequestDTO;
import com.techrepair.api.dto.OrdemServicoResponseDTO;
import com.techrepair.api.dto.OrdemServicoStatusDTO;
import com.techrepair.api.entity.StatusOrdemServico;
import com.techrepair.api.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço", description = "Ciclo de vida da Ordem de Serviço, diagnóstico, orçamento de peças e máquina de estados")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @PostMapping
    @Operation(
            summary = "Abrir nova Ordem de Serviço",
            description = "Abre uma OS validando se o equipamento pertence ao cliente informado, gerando número único automático e status inicial ABERTA."
    )
    public ResponseEntity<OrdemServicoResponseDTO> abrir(@Valid @RequestBody OrdemServicoRequestDTO dto) {
        OrdemServicoResponseDTO criada = ordemServicoService.abrirOrdemServico(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @Operation(
            summary = "Listar Ordens de Serviço",
            description = "Lista todas as Ordens de Serviço ou filtra opcionalmente pelo status atual."
    )
    public ResponseEntity<List<OrdemServicoResponseDTO>> listar(
            @RequestParam(required = false) StatusOrdemServico status) {
        return ResponseEntity.ok(ordemServicoService.listar(status));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar Ordem de Serviço por ID",
            description = "Retorna os dados completos e valores atualizados de uma Ordem de Serviço."
    )
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordemServicoService.buscarPorId(id));
    }

    @GetMapping("/numero/{numeroOs}")
    @Operation(
            summary = "Buscar Ordem de Serviço pelo número",
            description = "Busca uma Ordem de Serviço pelo seu código identificador único (ex: OS-20261021-A1B2C3)."
    )
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorNumeroOs(@PathVariable String numeroOs) {
        return ResponseEntity.ok(ordemServicoService.buscarPorNumeroOs(numeroOs));
    }

    @PatchMapping("/{id}/diagnostico")
    @Operation(
            summary = "Atualizar diagnóstico técnico e mão de obra",
            description = "Registra o laudo técnico e o valor da mão de obra, recalculando automaticamente o valor total da OS."
    )
    public ResponseEntity<OrdemServicoResponseDTO> atualizarDiagnostico(
            @PathVariable Long id,
            @Valid @RequestBody OrdemServicoDiagnosticoDTO dto) {
        return ResponseEntity.ok(ordemServicoService.atualizarDiagnostico(id, dto));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Avançar ou alterar status da Ordem de Serviço",
            description = "Executa transições válidas na máquina de estados. Ao transitar de AGUARDANDO_APROVACAO para EM_CONSERTO, realiza a baixa automática no estoque das peças vinculadas."
    )
    public ResponseEntity<OrdemServicoResponseDTO> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrdemServicoStatusDTO dto) {
        return ResponseEntity.ok(ordemServicoService.alterarStatus(id, dto));
    }

    @PostMapping("/{osId}/itens")
    @Operation(
            summary = "Adicionar peça ao orçamento da OS",
            description = "Adiciona uma peça ativa com saldo disponível à OS, congelando o preço unitário de venda e recalculando valorPecas e valorTotal."
    )
    public ResponseEntity<ItemOrdemServicoResponseDTO> adicionarItem(
            @PathVariable Long osId,
            @Valid @RequestBody ItemOrdemServicoRequestDTO dto) {
        ItemOrdemServicoResponseDTO criado = ordemServicoService.adicionarItem(osId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/{osId}/itens")
    @Operation(
            summary = "Listar itens (peças) da Ordem de Serviço",
            description = "Retorna todas as peças incluídas no orçamento da Ordem de Serviço."
    )
    public ResponseEntity<List<ItemOrdemServicoResponseDTO>> listarItens(@PathVariable Long osId) {
        return ResponseEntity.ok(ordemServicoService.listarItens(osId));
    }

    @DeleteMapping("/{osId}/itens/{itemId}")
    @Operation(
            summary = "Remover peça do orçamento da OS",
            description = "Remove um item da OS (enquanto ainda não entrou em conserto) e recalcula automaticamente os valores do orçamento."
    )
    public ResponseEntity<Void> removerItem(
            @PathVariable Long osId,
            @PathVariable Long itemId) {
        ordemServicoService.removerItem(osId, itemId);
        return ResponseEntity.noContent().build();
    }
}
