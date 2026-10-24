package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente,Long> {

    Optional<Cliente> findByPersonaNumeroDocumento(String numeroDocumento);

    boolean existsByPersonaNumeroDocumento(String numeroDocumento);
}
