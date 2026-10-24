package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
