package com.techrepair.api.controller;

import com.techrepair.api.dto.OrdemServicoDiagnosticoDTO;
import com.techrepair.api.dto.OrdemServicoRequestDTO;
import com.techrepair.api.dto.OrdemServicoResponseDTO;
import com.techrepair.api.dto.OrdemServicoStatusDTO;
import com.techrepair.api.entity.StatusOrdemServico;
import com.techrepair.api.service.OrdemServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @PostMapping
    public ResponseEntity<OrdemServicoResponseDTO> abrir(@Valid @RequestBody OrdemServicoRequestDTO dto) {
        OrdemServicoResponseDTO criada = ordemServicoService.abrirOrdemServico(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<OrdemServicoResponseDTO>> listar(
            @RequestParam(required = false) StatusOrdemServico status) {
        return ResponseEntity.ok(ordemServicoService.listar(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordemServicoService.buscarPorId(id));
    }

    @GetMapping("/numero/{numeroOs}")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorNumeroOs(@PathVariable String numeroOs) {
        return ResponseEntity.ok(ordemServicoService.buscarPorNumeroOs(numeroOs));
    }

    @PatchMapping("/{id}/diagnostico")
    public ResponseEntity<OrdemServicoResponseDTO> atualizarDiagnostico(
            @PathVariable Long id,
            @Valid @RequestBody OrdemServicoDiagnosticoDTO dto) {
        return ResponseEntity.ok(ordemServicoService.atualizarDiagnostico(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrdemServicoResponseDTO> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrdemServicoStatusDTO dto) {
        return ResponseEntity.ok(ordemServicoService.alterarStatus(id, dto));
    }
}
