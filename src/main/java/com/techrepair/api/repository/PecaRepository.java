package com.techrepair.api.repository;

import com.techrepair.api.entity.Peca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PecaRepository extends JpaRepository<Peca, Long> {

    boolean existsByCodigoSku(String codigoSku);

    Optional<Peca> findByCodigoSku(String codigoSku);

    @Query("SELECT p FROM Peca p WHERE p.quantidadeEstoque <= p.estoqueMinimo AND p.ativo = true")
    List<Peca> findPecasComEstoqueBaixo();
}
