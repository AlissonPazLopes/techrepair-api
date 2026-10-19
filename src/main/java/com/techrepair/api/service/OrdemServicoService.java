package com.techrepair.api.service;

import com.techrepair.api.dto.ItemOrdemServicoRequestDTO;
import com.techrepair.api.dto.ItemOrdemServicoResponseDTO;
import com.techrepair.api.dto.MovimentacaoEstoqueDTO;
import com.techrepair.api.dto.OrdemServicoDiagnosticoDTO;
import com.techrepair.api.dto.OrdemServicoRequestDTO;
import com.techrepair.api.dto.OrdemServicoResponseDTO;
import com.techrepair.api.dto.OrdemServicoStatusDTO;
import com.techrepair.api.entity.Cliente;
import com.techrepair.api.entity.Equipamento;
import com.techrepair.api.entity.ItemOrdemServico;
import com.techrepair.api.entity.OrdemServico;
import com.techrepair.api.entity.Peca;
import com.techrepair.api.entity.StatusOrdemServico;
import com.techrepair.api.repository.EquipamentoRepository;
import com.techrepair.api.repository.ItemOrdemServicoRepository;
import com.techrepair.api.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ItemOrdemServicoRepository itemOrdemServicoRepository;
    private final ClienteService clienteService;
    private final EquipamentoRepository equipamentoRepository;
    private final PecaService pecaService;

    @Transactional
    public OrdemServicoResponseDTO abrirOrdemServico(OrdemServicoRequestDTO dto) {
        Cliente cliente = clienteService.buscarEntidadePorId(dto.clienteId());

        Equipamento equipamento = equipamentoRepository.findById(dto.equipamentoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Equipamento não encontrado com o ID: " + dto.equipamentoId()));

        if (!equipamento.getCliente().getId().equals(cliente.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O equipamento informado não pertence ao cliente informado na Ordem de Serviço"
            );
        }

        OrdemServico os = OrdemServico.builder()
                .numeroOs(gerarNumeroOsUnico())
                .cliente(cliente)
                .equipamento(equipamento)
                .defeitoRelatado(dto.defeitoRelatado())
                .observacoes(dto.observacoes())
                .status(StatusOrdemServico.ABERTA)
                .valorMaoDeObra(BigDecimal.ZERO)
                .valorPecas(BigDecimal.ZERO)
                .valorTotal(BigDecimal.ZERO)
                .build();

        OrdemServico salva = ordemServicoRepository.save(os);
        return OrdemServicoResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listar(StatusOrdemServico status) {
        List<OrdemServico> ordens = (status != null)
                ? ordemServicoRepository.findByStatus(status)
                : ordemServicoRepository.findAll();

        return ordens.stream()
                .map(OrdemServicoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorId(Long id) {
        OrdemServico os = buscarEntidadePorId(id);
        return OrdemServicoResponseDTO.fromEntity(os);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorNumeroOs(String numeroOs) {
        OrdemServico os = ordemServicoRepository.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ordem de Serviço não encontrada com o número: " + numeroOs));
        return OrdemServicoResponseDTO.fromEntity(os);
    }

    @Transactional
    public OrdemServicoResponseDTO atualizarDiagnostico(Long id, OrdemServicoDiagnosticoDTO dto) {
        OrdemServico os = buscarEntidadePorId(id);

        if (os.getStatus().isEstadoFinal()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é permitido atualizar o diagnóstico de uma Ordem de Serviço com status " + os.getStatus()
            );
        }

        os.setDiagnosticoTecnico(dto.diagnosticoTecnico());
        os.setValorMaoDeObra(dto.valorMaoDeObra());
        if (dto.observacoes() != null) {
            os.setObservacoes(dto.observacoes());
        }
        os.recalcularValorTotal();

        OrdemServico atualizada = ordemServicoRepository.save(os);
        return OrdemServicoResponseDTO.fromEntity(atualizada);
    }

    @Transactional
    public ItemOrdemServicoResponseDTO adicionarItem(Long osId, ItemOrdemServicoRequestDTO dto) {
        OrdemServico os = buscarEntidadePorId(osId);

        if (!permiteAlterarItens(os.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é permitido adicionar peças em uma Ordem de Serviço com status " + os.getStatus()
            );
        }

        Peca peca = pecaService.buscarEntidadePorId(dto.pecaId());

        if (!Boolean.TRUE.equals(peca.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A peça '" + peca.getNome() + "' está inativa e não pode ser adicionada à Ordem de Serviço"
            );
        }

        if (dto.quantidade() > peca.getQuantidadeEstoque()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Estoque insuficiente para a peça '%s'. Disponível: %d, Solicitado: %d",
                            peca.getNome(), peca.getQuantidadeEstoque(), dto.quantidade())
            );
        }

        BigDecimal precoCongelado = peca.getPrecoVenda();
        BigDecimal subtotal = precoCongelado.multiply(BigDecimal.valueOf(dto.quantidade()));

        ItemOrdemServico item = ItemOrdemServico.builder()
                .ordemServico(os)
                .peca(peca)
                .quantidade(dto.quantidade())
                .precoUnitario(precoCongelado)
                .subtotal(subtotal)
                .build();

        ItemOrdemServico salvo = itemOrdemServicoRepository.save(item);
        recalcularOrcamentoPecas(os);

        return ItemOrdemServicoResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<ItemOrdemServicoResponseDTO> listarItens(Long osId) {
        buscarEntidadePorId(osId);
        return itemOrdemServicoRepository.findByOrdemServicoId(osId)
                .stream()
                .map(ItemOrdemServicoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void removerItem(Long osId, Long itemId) {
        OrdemServico os = buscarEntidadePorId(osId);

        if (!permiteAlterarItens(os.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é permitido remover peças de uma Ordem de Serviço com status " + os.getStatus()
            );
        }

        ItemOrdemServico item = itemOrdemServicoRepository.findByIdAndOrdemServicoId(itemId, osId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item não encontrado nesta Ordem de Serviço"));

        itemOrdemServicoRepository.delete(item);
        itemOrdemServicoRepository.flush();
        recalcularOrcamentoPecas(os);
    }

    @Transactional
    public OrdemServicoResponseDTO alterarStatus(Long id, OrdemServicoStatusDTO dto) {
        OrdemServico os = buscarEntidadePorId(id);
        StatusOrdemServico statusAtual = os.getStatus();
        StatusOrdemServico novoStatus = dto.status();

        if (!statusAtual.podeTransitarPara(novoStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Transição de status inválida: não é permitido alterar de %s para %s",
                            statusAtual, novoStatus)
            );
        }

        // Baixa automática de estoque ao aprovar o orçamento (AGUARDANDO_APROVACAO -> EM_CONSERTO)
        if (statusAtual == StatusOrdemServico.AGUARDANDO_APROVACAO
                && novoStatus == StatusOrdemServico.EM_CONSERTO) {
            List<ItemOrdemServico> itens = itemOrdemServicoRepository.findByOrdemServicoId(os.getId());
            for (ItemOrdemServico item : itens) {
                pecaService.registrarBaixaEstoque(
                        item.getPeca().getId(),
                        new MovimentacaoEstoqueDTO(item.getQuantidade())
                );
            }
        }

        os.setStatus(novoStatus);
        if (dto.observacoes() != null && !dto.observacoes().isBlank()) {
            os.setObservacoes(dto.observacoes());
        }

        if (novoStatus == StatusOrdemServico.FINALIZADA) {
            os.setDataFechamento(LocalDateTime.now());
        }

        OrdemServico atualizada = ordemServicoRepository.save(os);
        return OrdemServicoResponseDTO.fromEntity(atualizada);
    }

    @Transactional(readOnly = true)
    public OrdemServico buscarEntidadePorId(Long id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ordem de Serviço não encontrada com o ID: " + id));
    }

    private boolean permiteAlterarItens(StatusOrdemServico status) {
        return status == StatusOrdemServico.ABERTA
                || status == StatusOrdemServico.EM_ANALISE
                || status == StatusOrdemServico.AGUARDANDO_APROVACAO;
    }

    private void recalcularOrcamentoPecas(OrdemServico os) {
        BigDecimal somaPecas = itemOrdemServicoRepository.findByOrdemServicoId(os.getId())
                .stream()
                .map(ItemOrdemServico::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        os.setValorPecas(somaPecas);
        os.recalcularValorTotal();
        ordemServicoRepository.save(os);
    }

    private String gerarNumeroOsUnico() {
        String dataPrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String numeroGerado;
        do {
            String sufixoAleatorio = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            numeroGerado = "OS-" + dataPrefix + "-" + sufixoAleatorio;
        } while (ordemServicoRepository.existsByNumeroOs(numeroGerado));
        return numeroGerado;
    }
}
