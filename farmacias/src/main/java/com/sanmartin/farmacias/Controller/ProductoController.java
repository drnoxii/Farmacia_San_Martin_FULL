package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.ProductoDto;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.IProductoServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/producto"})
@Tag(name = "Productos", description = "CRUD de Productos")
public class ProductoController {
    private final IProductoServices productoServices;

    public ProductoController(IProductoServices productoServices) {
        this.productoServices = productoServices;
    }

    @GetMapping
    @Operation(summary = "Lista todos los productos disponibles")
    public ResponseEntity<List<ProductoDto>> listarProductos() {
        return ResponseEntity.ok(this.productoServices.listarProductos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca el producto con ID especifico")
    public ResponseEntity<ProductoDto> buscarPorId(@PathVariable Long id) {
        return productoServices.buscarProductoPorId(id).map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("NO se encontró el producto con ID: " + id));
    }

    @GetMapping("/nombre/{nombre}")
    @Operation(summary = "Buscar productos por nombre")
    public ResponseEntity<ProductoDto> buscarPorNombre(@PathVariable String nombre) {
        return productoServices.buscarPorNombre(nombre).map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("NO se encontró el producto con nombre: " + nombre));
    }

    @GetMapping("/categoria/{idCategoria}")
    @Operation(summary = "Buscar productos por categoria")
    public ResponseEntity<List<ProductoDto>> buscarPorCategoria(@PathVariable Long idCategoria) {
        return ResponseEntity.ok(productoServices.buscarPorCategoria(idCategoria));
    }

    @GetMapping("/laboratorio/{idLaboratorio}")
    @Operation(summary = "Buscar productos por laboratorio")
    public ResponseEntity<List<ProductoDto>> buscarPorLaboratorio(@PathVariable Long idLaboratorio) {
        return ResponseEntity.ok(productoServices.buscarPorLaboratorio(idLaboratorio));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Busca  productos por su estado")
    public ResponseEntity<List<ProductoDto>> buscarPorEstado(@PathVariable EstadoGeneral estado) {
        return ResponseEntity.ok(productoServices.buscarPorEstadoGeneral(estado));
    }

    @PostMapping
    @Operation(summary = "Se registra el Producto")
    public ResponseEntity<ProductoDto> registrarProducto(@RequestBody @Valid ProductoDto producto) {
        return new ResponseEntity(productoServices.registrarProducto(producto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza el registro de los productos")
    public ResponseEntity<ProductoDto> actualizarProducto(@PathVariable Long id, @RequestBody @Valid ProductoDto producto) {
        return (ResponseEntity) this.productoServices.actualizarProducto(id, producto).map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("NO se pudo actualizar el producto con ID: " + id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Se elimina el producto con el ID dado")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id){
        if (productoServices.eliminarProducto(id)){
            return ResponseEntity.noContent().build();
        }
        throw new ResourceNotFoundException("No se pudo eliminar el producto con ID: "+id);
    }
}
