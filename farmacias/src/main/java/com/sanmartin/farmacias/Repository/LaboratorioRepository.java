package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {
}