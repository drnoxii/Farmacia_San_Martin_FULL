package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Dto.InventarioDto;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByNombreProducto(String nombre);
    Optional<Producto> findBynombreProducto(String nombreProducto);
    List<Producto> findByCategoria_idCategoria(Long idCategoria);
    List<Producto> findByLaboratorio_idLaboratorio(Long idLaboratorio);
    List<Producto> findByEstadoGeneral(EstadoGeneral estadoGeneral);

    @Query("""
    SELECT new com.sanmartin.farmacias.Dto.InventarioDto(
        p.idProducto,
        p.codigo,
        p.nombreProducto,
        p.descripcion,
        p.categoria.idCategoria,
        p.categoria.nombreCategoria,
        p.laboratorio.idLaboratorio,
        p.laboratorio.nombreLaboratorio,
        p.precioCompra,
        p.precioVenta,
        p.stockMinimo,
        p.estadoGeneral,
        COALESCE(SUM(l.stockLote), 0L)
    )
    FROM Producto p
    LEFT JOIN Lote l ON l.producto = p
    GROUP BY p.idProducto, p.codigo, p.nombreProducto, p.descripcion, 
             p.categoria.idCategoria, p.categoria.nombreCategoria, 
             p.laboratorio.idLaboratorio, p.laboratorio.nombreLaboratorio, 
             p.precioCompra, p.precioVenta, p.stockMinimo, p.estadoGeneral
    ORDER BY p.nombreProducto
    """)
    List<InventarioDto> consultarInventario();
}
