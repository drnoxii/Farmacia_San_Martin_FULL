package com.sanmartin.farmacias.Controller;


import com.sanmartin.farmacias.Dto.CompraRequestDTO;
import com.sanmartin.farmacias.Dto.CompraResponseDTO;
import com.sanmartin.farmacias.Entity.EstadoCompra;
import com.sanmartin.farmacias.Entity.Proveedor;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.ICompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compras")
@Tag(name = "Compras", description = "Registro y consulta de compras")
public class CompraController {

    private final ICompraService compraService;

    public CompraController(ICompraService compraService) {
        this.compraService = compraService;
    }


    @GetMapping
    @Operation(summary = "Listar todas las compras")
    public ResponseEntity<List<CompraResponseDTO>> listarTodo() {
        return ResponseEntity.ok(compraService.listarTodo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener compra por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<CompraResponseDTO> obtener(@PathVariable Long id) {
        return compraService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compra no encontrada con ID: " + id));
    }

    @GetMapping("/proveedor/{idProveedor}")
    @Operation(summary = "Busca las compras por Proveedor")
    public ResponseEntity<List<CompraResponseDTO>> buscarPorProveedor(@PathVariable Long idProveedor){
        return ResponseEntity.ok(compraService.listarPorIdProveedor(idProveedor));
    }

    @GetMapping("/proveedor/nombre/{nombreProveedor}")
    @Operation(summary = "Busca por nombre. Para la búsqueda de proveedores por nombre")
    public ResponseEntity<List<CompraResponseDTO>> buscarPorNombreProveedor(@PathVariable String nombreProveedor){
        return ResponseEntity.ok(compraService.listarPorNombreProveedor(nombreProveedor));
    }

    @PostMapping
    @Operation(summary = "Registrar compra (crea detalles y lotes)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compra registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proveedor, usuario o producto no encontrados")
    })
    public ResponseEntity<CompraResponseDTO> registrar(
            @RequestBody @Valid CompraRequestDTO dto) {
        return new ResponseEntity<>(compraService.registrar(dto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el Estado de la Compra")
    public ResponseEntity<CompraResponseDTO> actualizarEstado(@PathVariable Long id, @RequestParam EstadoCompra estadoCompra){
        return ResponseEntity.ok(compraService.actualizarEstado(id, estadoCompra));
    }
}
