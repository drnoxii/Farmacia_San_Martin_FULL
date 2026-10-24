package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra,Long> {
    List<Compra> findByProveedorIdProveedor(Long idProveedor);
}
