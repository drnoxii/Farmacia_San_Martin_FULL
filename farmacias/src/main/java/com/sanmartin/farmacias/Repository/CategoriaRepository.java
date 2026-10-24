package com.sanmartin.farmacias.Repository;

import com.sanmartin.farmacias.Entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombreCategoria(String nombre);
    Optional<Categoria> findByNombreCategoria(String nombre);
}
