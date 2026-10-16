package com.techrepair.api.repository;

import com.techrepair.api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);

    boolean existsByDocumento(String documento);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByDocumento(String documento);
}
