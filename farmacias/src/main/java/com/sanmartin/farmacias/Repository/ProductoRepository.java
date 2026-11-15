package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Dto.InventarioDto;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.EstadoLote;
import com.sanmartin.farmacias.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Query("""
    SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
    FROM Producto p
    WHERE LOWER(TRIM(p.nombreProducto)) = LOWER(TRIM(:nombre))
      AND p.laboratorio.idLaboratorio = :idLaboratorio
      AND LOWER(TRIM(p.presentacion)) = LOWER(TRIM(:presentacion))
      AND p.idProducto <> :idExcluido
    """)
    boolean existeProductoDuplicado(
            @Param("nombre") String nombre,
            @Param("idLaboratorio") Long idLaboratorio,
            @Param("presentacion") String presentacion,
            @Param("idExcluido") Long idExcluido
    );
    List<Producto> findByNombreProductoIgnoreCase(String nombreProducto);
    List<Producto> findByCategoria_idCategoria(Long idCategoria);
    List<Producto> findByLaboratorio_idLaboratorio(Long idLaboratorio);
    List<Producto> findByEstadoGeneral(EstadoGeneral estadoGeneral);

    @Query("""
    SELECT new com.sanmartin.farmacias.Dto.InventarioDto(
        p.idProducto,
        p.codigo,
        p.nombreProducto,
        p.descripcion,
        p.presentacion,
        p.categoria.idCategoria,
        p.categoria.nombreCategoria,
        p.laboratorio.idLaboratorio,
        p.laboratorio.nombreLaboratorio,
        p.precioVenta,
        p.stockMinimo,
        p.estadoGeneral,
        COALESCE(SUM(
            CASE
                WHEN p.estadoGeneral = :estadoProductoActivo
                 AND l.estado = :estadoLoteActivo
                 AND l.fechaVencimiento >= :hoy
                 AND l.stockLote > 0
                THEN l.stockLote
                ELSE 0
            END
        ), 0L)
    )
    FROM Producto p
    LEFT JOIN Lote l ON l.producto = p
    GROUP BY p.idProducto, p.codigo, p.nombreProducto,
             p.descripcion, p.presentacion,
             p.categoria.idCategoria, p.categoria.nombreCategoria,
             p.laboratorio.idLaboratorio, p.laboratorio.nombreLaboratorio,
             p.precioVenta, p.stockMinimo, p.estadoGeneral
    ORDER BY p.nombreProducto
    """)
    List<InventarioDto> consultarInventario(
            @Param("estadoProductoActivo") EstadoGeneral estadoProductoActivo,
            @Param("estadoLoteActivo") EstadoLote estadoLoteActivo,
            @Param("hoy") LocalDate hoy
    );
}
