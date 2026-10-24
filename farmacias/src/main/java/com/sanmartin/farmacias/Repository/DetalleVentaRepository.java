package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findByVenta_idVenta(Long idVenta);
}
