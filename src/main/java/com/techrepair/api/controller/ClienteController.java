package com.techrepair.api.controller;

import com.techrepair.api.dto.ClienteRequestDTO;
import com.techrepair.api.dto.ClienteResponseDTO;
import com.techrepair.api.dto.EquipamentoResponseDTO;
import com.techrepair.api.service.ClienteService;
import com.techrepair.api.service.EquipamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final EquipamentoService equipamentoService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrar(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO criado = clienteService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/{id}/equipamentos")
    public ResponseEntity<List<EquipamentoResponseDTO>> listarEquipamentosDoCliente(@PathVariable Long id) {
        return ResponseEntity.ok(equipamentoService.listarPorCliente(id));
    }
}
