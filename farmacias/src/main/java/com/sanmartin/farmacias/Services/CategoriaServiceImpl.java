package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.CategoriaDto;
import com.sanmartin.farmacias.Entity.Categoria;
import com.sanmartin.farmacias.Exception.DuplicateResourceException;
import com.sanmartin.farmacias.Repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements ICategoriaServices{
    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDto> listarCategorias() {
        return this.categoriaRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<CategoriaDto> buscarCategoriaPorId(Long id) {
        return this.categoriaRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional
    public CategoriaDto registrarCategoria(CategoriaDto dto) {
        if(categoriaRepository.existsByNombreCategoria(dto.nombreCategoria())){
            throw new DuplicateResourceException("Ya existe una categoria con ese nombre.");
        }

        //Creamos la categoria y la "guardamos"
        Categoria categoria = new Categoria();
        categoria.setNombreCategoria(dto.nombreCategoria());
        categoria.setDescripcionCate(dto.descripCategoria());
        categoria= categoriaRepository.save(categoria);
        return convertToDto(categoria);
    }

    @Override
    @Transactional
    public Optional<CategoriaDto> actualizarCategoria(Long id, CategoriaDto dto) {
        return this.categoriaRepository.findById(id).map(cat -> {
            cat.setNombreCategoria(dto.nombreCategoria());
            cat.setDescripcionCate(dto.descripCategoria());
            categoriaRepository.save(cat);
            return convertToDto(cat);
        });
    }

    @Override
    public Optional<CategoriaDto> buscarPorNombre(String nombre) {
        return this.categoriaRepository.findByNombreCategoria(nombre).map(this::convertToDto);
    }

    private CategoriaDto convertToDto (Categoria categoria){
        return new CategoriaDto(
                categoria.getIdCategoria(),
                categoria.getNombreCategoria(),
                categoria.getDescripcionCate()
        );
    }
}
