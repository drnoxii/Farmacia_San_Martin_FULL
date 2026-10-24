package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.Caja;
import com.sanmartin.farmacias.Entity.EstadoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja,Long> {

    // Para saber si un usuario ya tiene caja abierta
    Optional<Caja> findByUsuarioIdUsuarioAndEstado(Long idUsuario, EstadoCaja estado);

    // Listar por estado
    List<Caja> findByEstado(EstadoCaja estado);

    // Listar por usuario
    List<Caja> findByUsuarioIdUsuario(Long idUsuario);
}
