package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.CategoriaDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ICategoriaServices {
    List<CategoriaDto> listarCategorias();
    Optional<CategoriaDto> buscarCategoriaPorId(Long id);
    CategoriaDto registrarCategoria (CategoriaDto dto);
    Optional<CategoriaDto> actualizarCategoria(Long id, CategoriaDto dto);

    Optional<CategoriaDto> buscarPorNombre(String nombre);

}
