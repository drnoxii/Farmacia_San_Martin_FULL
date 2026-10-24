package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.EstadoLote;
import com.sanmartin.farmacias.Entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    // Lotes de un producto
    List<Lote> findByProductoIdProducto(Long idProducto);

    // Lotes por estado
    List<Lote> findByEstado(EstadoLote estado);

    // Lotes de un producto por estado
    List<Lote> findByProductoIdProductoAndEstado(Long idProducto, EstadoLote estado);

    // Lotes activos ordenados por vencimiento (para FEFO)
    List<Lote> findByProductoIdProductoAndEstadoOrderByFechaVencimientoAsc(
            Long idProducto, EstadoLote estado);

    // Lotes que vencen antes de una fecha
    @Query("""
        SELECT l FROM Lote l
        WHERE l.fechaVencimiento <= :fecha
          AND l.estado = 'ACTIVO'
        ORDER BY l.fechaVencimiento ASC
        """)
    List<Lote> findPorVencerAntesDe(@Param("fecha") LocalDate fecha);

    Optional<Lote> findByDetalleCompraIdDetalleCompra(Long idDetalleCompra);
}
