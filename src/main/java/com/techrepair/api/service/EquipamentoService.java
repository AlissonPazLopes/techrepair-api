package com.techrepair.api.service;

import com.techrepair.api.dto.EquipamentoRequestDTO;
import com.techrepair.api.dto.EquipamentoResponseDTO;
import com.techrepair.api.entity.Cliente;
import com.techrepair.api.entity.Equipamento;
import com.techrepair.api.repository.EquipamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final ClienteService clienteService;

    @Transactional
    public EquipamentoResponseDTO cadastrar(EquipamentoRequestDTO dto) {
        Cliente cliente = clienteService.buscarEntidadePorId(dto.clienteId());

        Equipamento equipamento = Equipamento.builder()
                .cliente(cliente)
                .tipo(dto.tipo())
                .marca(dto.marca())
                .modelo(dto.modelo())
                .numeroSerie(dto.numeroSerie())
                .observacoes(dto.observacoes())
                .build();

        Equipamento salvo = equipamentoRepository.save(equipamento);
        return EquipamentoResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<EquipamentoResponseDTO> listarTodos() {
        return equipamentoRepository.findAll()
                .stream()
                .map(EquipamentoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipamentoResponseDTO> listarPorCliente(Long clienteId) {
        clienteService.buscarEntidadePorId(clienteId);
        return equipamentoRepository.findByClienteId(clienteId)
                .stream()
                .map(EquipamentoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipamentoResponseDTO buscarPorId(Long id) {
        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Equipamento não encontrado com o ID: " + id));
        return EquipamentoResponseDTO.fromEntity(equipamento);
    }
}
