package com.techrepair.api.controller;

import com.techrepair.api.dto.EquipamentoRequestDTO;
import com.techrepair.api.dto.EquipamentoResponseDTO;
import com.techrepair.api.service.EquipamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipamentos")
@RequiredArgsConstructor
@Tag(name = "Equipamentos", description = "Gerenciamento de equipamentos vinculados aos clientes")
public class EquipamentoController {

    private final EquipamentoService equipamentoService;

    @PostMapping
    @Operation(
            summary = "Cadastrar novo equipamento",
            description = "Registra um equipamento (notebook, smartphone, console, etc.) vinculando-o a um cliente existente."
    )
    public ResponseEntity<EquipamentoResponseDTO> cadastrar(@Valid @RequestBody EquipamentoRequestDTO dto) {
        EquipamentoResponseDTO criado = equipamentoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    @Operation(
            summary = "Listar equipamentos",
            description = "Retorna todos os equipamentos cadastrados ou filtra pelos equipamentos de um cliente específico via query parameter clienteId."
    )
    public ResponseEntity<List<EquipamentoResponseDTO>> listar(
            @RequestParam(required = false) Long clienteId) {
        if (clienteId != null) {
            return ResponseEntity.ok(equipamentoService.listarPorCliente(clienteId));
        }
        return ResponseEntity.ok(equipamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar equipamento por ID",
            description = "Retorna os detalhes de um equipamento a partir do seu identificador."
    )
    public ResponseEntity<EquipamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipamentoService.buscarPorId(id));
    }
}
