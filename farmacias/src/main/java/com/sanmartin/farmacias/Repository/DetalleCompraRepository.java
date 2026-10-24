package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCompraRepository extends JpaRepository<DetalleCompra,Long> {
    List<DetalleCompra> findByCompraIdCompra(Long idCompra);
}
