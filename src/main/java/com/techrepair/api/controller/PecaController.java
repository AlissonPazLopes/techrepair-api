package com.techrepair.api.controller;

import com.techrepair.api.dto.MovimentacaoEstoqueDTO;
import com.techrepair.api.dto.PecaRequestDTO;
import com.techrepair.api.dto.PecaResponseDTO;
import com.techrepair.api.service.PecaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pecas")
@RequiredArgsConstructor
@Tag(name = "Peças e Estoque", description = "Catálogo de peças de reposição e movimentação de estoque")
public class PecaController {

    private final PecaService pecaService;

    @PostMapping
    @Operation(
            summary = "Cadastrar nova peça",
            description = "Cadastra uma nova peça no catálogo validando código SKU único, preços e estoque mínimo."
    )
    public ResponseEntity<PecaResponseDTO> cadastrar(@Valid @RequestBody PecaRequestDTO dto) {
        PecaResponseDTO criada = pecaService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @Operation(
            summary = "Listar todas as peças",
            description = "Retorna todas as peças cadastradas no catálogo com indicador de alerta de estoque mínimo."
    )
    public ResponseEntity<List<PecaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(pecaService.listarTodas());
    }

    @GetMapping("/estoque-baixo")
    @Operation(
            summary = "Listar peças com estoque baixo",
            description = "Retorna todas as peças ativas cuja quantidade em estoque é menor ou igual ao estoque mínimo definido."
    )
    public ResponseEntity<List<PecaResponseDTO>> listarEstoqueBaixo() {
        return ResponseEntity.ok(pecaService.listarEstoqueBaixo());
    }

    @GetMapping("/sku/{codigoSku}")
    @Operation(
            summary = "Buscar peça por código SKU",
            description = "Busca uma peça específica pelo seu código SKU único."
    )
    public ResponseEntity<PecaResponseDTO> buscarPorSku(@PathVariable String codigoSku) {
        return ResponseEntity.ok(pecaService.buscarPorSku(codigoSku));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar peça por ID",
            description = "Retorna os detalhes de uma peça a partir do seu identificador."
    )
    public ResponseEntity<PecaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pecaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar dados de uma peça",
            description = "Atualiza as informações cadastrais e preços de uma peça existente."
    )
    public ResponseEntity<PecaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PecaRequestDTO dto) {
        return ResponseEntity.ok(pecaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Inativar peça",
            description = "Realiza a exclusão lógica (inativação) de uma peça do catálogo."
    )
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        pecaService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estoque/entrada")
    @Operation(
            summary = "Registrar entrada de estoque",
            description = "Adiciona a quantidade informada ao saldo atual em estoque da peça."
    )
    public ResponseEntity<PecaResponseDTO> registrarEntradaEstoque(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueDTO dto) {
        return ResponseEntity.ok(pecaService.registrarEntradaEstoque(id, dto));
    }

    @PatchMapping("/{id}/estoque/baixa")
    @Operation(
            summary = "Registrar baixa manual de estoque",
            description = "Subtrai a quantidade informada do estoque da peça, bloqueando a operação caso o saldo seja insuficiente."
    )
    public ResponseEntity<PecaResponseDTO> registrarBaixaEstoque(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueDTO dto) {
        return ResponseEntity.ok(pecaService.registrarBaixaEstoque(id, dto));
    }
}
