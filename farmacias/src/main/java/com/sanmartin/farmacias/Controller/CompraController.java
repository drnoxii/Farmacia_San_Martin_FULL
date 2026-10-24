package com.sanmartin.farmacias.Controller;


import com.sanmartin.farmacias.Dto.CompraRequestDTO;
import com.sanmartin.farmacias.Dto.CompraResponseDTO;
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
}
