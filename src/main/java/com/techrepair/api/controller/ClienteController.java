package com.techrepair.api.controller;

import com.techrepair.api.dto.ClienteRequestDTO;
import com.techrepair.api.dto.ClienteResponseDTO;
import com.techrepair.api.dto.EquipamentoResponseDTO;
import com.techrepair.api.service.ClienteService;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes da assistência técnica")
public class ClienteController {

    private final ClienteService clienteService;
    private final EquipamentoService equipamentoService;

    @PostMapping
    @Operation(
            summary = "Cadastrar novo cliente",
            description = "Registra um novo cliente validando unicidade de CPF/CNPJ e e-mail."
    )
    public ResponseEntity<ClienteResponseDTO> cadastrar(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO criado = clienteService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os clientes",
            description = "Retorna a lista de todos os clientes cadastrados no sistema."
    )
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar cliente por ID",
            description = "Retorna os dados detalhados de um cliente a partir do seu identificador."
    )
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/{id}/equipamentos")
    @Operation(
            summary = "Listar equipamentos de um cliente",
            description = "Retorna todos os equipamentos vinculados a um cliente específico."
    )
    public ResponseEntity<List<EquipamentoResponseDTO>> listarEquipamentosDoCliente(@PathVariable Long id) {
        return ResponseEntity.ok(equipamentoService.listarPorCliente(id));
    }
}
