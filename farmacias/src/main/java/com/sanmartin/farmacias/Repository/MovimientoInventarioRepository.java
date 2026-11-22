package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface MovimientoInventarioRepository
        extends JpaRepository<MovimientoInventario, Long> {

    // Consultar movimientos del más reciente al más antiguo
    Page<MovimientoInventario>
    findAllByOrderByFechaDescIdMovimientoDesc(Pageable pageable);

}