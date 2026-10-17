package com.techrepair.api.controller;

import com.techrepair.api.dto.MovimentacaoEstoqueDTO;
import com.techrepair.api.dto.PecaRequestDTO;
import com.techrepair.api.dto.PecaResponseDTO;
import com.techrepair.api.service.PecaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pecas")
@RequiredArgsConstructor
public class PecaController {

    private final PecaService pecaService;

    @PostMapping
    public ResponseEntity<PecaResponseDTO> cadastrar(@Valid @RequestBody PecaRequestDTO dto) {
        PecaResponseDTO criada = pecaService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<PecaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(pecaService.listarTodas());
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<PecaResponseDTO>> listarEstoqueBaixo() {
        return ResponseEntity.ok(pecaService.listarEstoqueBaixo());
    }

    @GetMapping("/sku/{codigoSku}")
    public ResponseEntity<PecaResponseDTO> buscarPorSku(@PathVariable String codigoSku) {
        return ResponseEntity.ok(pecaService.buscarPorSku(codigoSku));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PecaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pecaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PecaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PecaRequestDTO dto) {
        return ResponseEntity.ok(pecaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        pecaService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estoque/entrada")
    public ResponseEntity<PecaResponseDTO> registrarEntradaEstoque(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueDTO dto) {
        return ResponseEntity.ok(pecaService.registrarEntradaEstoque(id, dto));
    }

    @PatchMapping("/{id}/estoque/baixa")
    public ResponseEntity<PecaResponseDTO> registrarBaixaEstoque(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueDTO dto) {
        return ResponseEntity.ok(pecaService.registrarBaixaEstoque(id, dto));
    }
}
