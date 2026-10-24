package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByCajaIdCaja(Long idCaja);

    List<Venta> findByUsuarioIdUsuario(Long idUsuario);

    long count();
}
