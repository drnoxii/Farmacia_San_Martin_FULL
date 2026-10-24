package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    boolean existsByRuc(String ruc);
}
