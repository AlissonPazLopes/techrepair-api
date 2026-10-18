package com.techrepair.api.repository;

import com.techrepair.api.entity.OrdemServico;
import com.techrepair.api.entity.StatusOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    Optional<OrdemServico> findByNumeroOs(String numeroOs);

    boolean existsByNumeroOs(String numeroOs);

    List<OrdemServico> findByStatus(StatusOrdemServico status);

    List<OrdemServico> findByClienteId(Long clienteId);
}
