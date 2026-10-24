package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.CategoriaDto;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.ICategoriaServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/categoria"})
@Tag(name = "Categoria", description = "CRUD de Categoria")
public class CategoriaController {
    private final ICategoriaServices categoriaServices;

    public CategoriaController(ICategoriaServices categoriaServices) {
        this.categoriaServices = categoriaServices;
    }

    @GetMapping
    @Operation(summary = "Lista todas las categorias disponibles")
    public ResponseEntity<List<CategoriaDto>> listarTodo(){
        return  ResponseEntity.ok(this.categoriaServices.listarCategorias());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Se busca la categoria con ID específico")
    public ResponseEntity<CategoriaDto> buscarPorId(@PathVariable Long id){
        return categoriaServices.buscarCategoriaPorId(id).map(ResponseEntity::ok).orElseThrow(() -> new ResourceNotFoundException("NO se encontró la categoria con ID: "+id));
    }

    @GetMapping("/nombre/{nombre}")
    @Operation(summary = "Se busca la categoria por nombre")
    public ResponseEntity<CategoriaDto> buscarPorNombre(@PathVariable String nombre){
        return categoriaServices.buscarPorNombre(nombre).map(ResponseEntity::ok).orElseThrow(() -> new ResourceNotFoundException("NO se encontró la categoria con nombre: "+nombre));
    }

    @PostMapping
    @Operation(summary = "Se crea la categoria")
    public ResponseEntity<CategoriaDto> registrarCategoria(@RequestBody @Valid CategoriaDto dto){
        return new ResponseEntity(categoriaServices.registrarCategoria(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Se actualiza la categoria con ID específico")
    public ResponseEntity<CategoriaDto> actualizarCategoria(@PathVariable Long id, @RequestBody @Valid CategoriaDto dto){
        return (ResponseEntity)this.categoriaServices.actualizarCategoria(id, dto).map(ResponseEntity::ok).orElseThrow(() -> new ResourceNotFoundException("NO se pudo actualizar la categoria con Id: "+id));
    }
}
