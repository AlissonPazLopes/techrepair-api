package com.techrepair.api.service;

import com.techrepair.api.dto.MovimentacaoEstoqueDTO;
import com.techrepair.api.dto.PecaRequestDTO;
import com.techrepair.api.dto.PecaResponseDTO;
import com.techrepair.api.entity.Peca;
import com.techrepair.api.repository.PecaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PecaService {

    private final PecaRepository pecaRepository;

    @Transactional
    public PecaResponseDTO cadastrar(PecaRequestDTO dto) {
        if (pecaRepository.existsByCodigoSku(dto.codigoSku())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma peça cadastrada com o código SKU: " + dto.codigoSku()
            );
        }

        Peca peca = Peca.builder()
                .nome(dto.nome())
                .codigoSku(dto.codigoSku())
                .descricao(dto.descricao())
                .precoCusto(dto.precoCusto())
                .precoVenda(dto.precoVenda())
                .quantidadeEstoque(dto.quantidadeEstoque() != null ? dto.quantidadeEstoque() : 0)
                .estoqueMinimo(dto.estoqueMinimo() != null ? dto.estoqueMinimo() : 5)
                .ativo(true)
                .build();

        Peca salva = pecaRepository.save(peca);
        return PecaResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public List<PecaResponseDTO> listarTodas() {
        return pecaRepository.findAll()
                .stream()
                .map(PecaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PecaResponseDTO buscarPorId(Long id) {
        Peca peca = buscarEntidadePorId(id);
        return PecaResponseDTO.fromEntity(peca);
    }

    @Transactional(readOnly = true)
    public PecaResponseDTO buscarPorSku(String codigoSku) {
        Peca peca = pecaRepository.findByCodigoSku(codigoSku)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Peça não encontrada com o SKU: " + codigoSku));
        return PecaResponseDTO.fromEntity(peca);
    }

    @Transactional
    public PecaResponseDTO atualizar(Long id, PecaRequestDTO dto) {
        Peca peca = buscarEntidadePorId(id);

        if (!peca.getCodigoSku().equals(dto.codigoSku()) && pecaRepository.existsByCodigoSku(dto.codigoSku())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outra peça cadastrada com o código SKU: " + dto.codigoSku()
            );
        }

        peca.setNome(dto.nome());
        peca.setCodigoSku(dto.codigoSku());
        peca.setDescricao(dto.descricao());
        peca.setPrecoCusto(dto.precoCusto());
        peca.setPrecoVenda(dto.precoVenda());
        if (dto.quantidadeEstoque() != null) {
            peca.setQuantidadeEstoque(dto.quantidadeEstoque());
        }
        if (dto.estoqueMinimo() != null) {
            peca.setEstoqueMinimo(dto.estoqueMinimo());
        }

        Peca atualizada = pecaRepository.save(peca);
        return PecaResponseDTO.fromEntity(atualizada);
    }

    @Transactional
    public void inativar(Long id) {
        Peca peca = buscarEntidadePorId(id);
        peca.setAtivo(false);
        pecaRepository.save(peca);
    }

    @Transactional
    public PecaResponseDTO registrarEntradaEstoque(Long id, MovimentacaoEstoqueDTO dto) {
        Peca peca = buscarEntidadePorId(id);
        peca.setQuantidadeEstoque(peca.getQuantidadeEstoque() + dto.quantidade());
        Peca atualizada = pecaRepository.save(peca);
        return PecaResponseDTO.fromEntity(atualizada);
    }

    @Transactional
    public PecaResponseDTO registrarBaixaEstoque(Long id, MovimentacaoEstoqueDTO dto) {
        Peca peca = buscarEntidadePorId(id);

        if (dto.quantidade() > peca.getQuantidadeEstoque()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Estoque insuficiente para a peça '%s' (SKU: %s). Disponível: %d, Solicitado: %d",
                            peca.getNome(), peca.getCodigoSku(), peca.getQuantidadeEstoque(), dto.quantidade())
            );
        }

        peca.setQuantidadeEstoque(peca.getQuantidadeEstoque() - dto.quantidade());
        Peca atualizada = pecaRepository.save(peca);
        return PecaResponseDTO.fromEntity(atualizada);
    }

    @Transactional(readOnly = true)
    public List<PecaResponseDTO> listarEstoqueBaixo() {
        return pecaRepository.findPecasComEstoqueBaixo()
                .stream()
                .map(PecaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Peca buscarEntidadePorId(Long id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Peça não encontrada com o ID: " + id));
    }
}
