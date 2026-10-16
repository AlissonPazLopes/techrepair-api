package com.techrepair.api.service;

import com.techrepair.api.dto.ClienteRequestDTO;
import com.techrepair.api.dto.ClienteResponseDTO;
import com.techrepair.api.entity.Cliente;
import com.techrepair.api.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO dto) {
        if (clienteRepository.existsByDocumento(dto.documento())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente cadastrado com este documento");
        }
        if (clienteRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente cadastrado com este e-mail");
        }

        Cliente cliente = Cliente.builder()
                .nome(dto.nome())
                .documento(dto.documento())
                .telefone(dto.telefone())
                .email(dto.email())
                .build();

        Cliente salvo = clienteRepository.save(cliente);
        return ClienteResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente cliente = buscarEntidadePorId(id);
        return ClienteResponseDTO.fromEntity(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarEntidadePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado com o ID: " + id));
    }
}
