package com.sanmartin.farmacias.Controller;


import com.sanmartin.farmacias.Dto.LoteDTO;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.ILoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "Consulta de lotes de productos")
public class LoteController {

    private final ILoteService loteServices;

    public LoteController(ILoteService loteServices) {
        this.loteServices = loteServices;
    }

    @GetMapping
    @Operation(summary = "Listar todos los lotes")
    public ResponseEntity<List<LoteDTO>> listarTodo() {
        return ResponseEntity.ok(loteServices.listarTodo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener lote por ID")
    public ResponseEntity<LoteDTO> obtener(@PathVariable Long id) {
        return loteServices.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + id));
    }

    @GetMapping("/producto/{idProducto}")
    @Operation(summary = "Listar lotes de un producto")
    public ResponseEntity<List<LoteDTO>> listarPorProducto(@PathVariable Long idProducto) {
        return ResponseEntity.ok(loteServices.listarPorProducto(idProducto));
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar solo lotes activos")
    public ResponseEntity<List<LoteDTO>> listarActivos() {
        return ResponseEntity.ok(loteServices.listarActivos());
    }

    @GetMapping("/por-vencer")
    @Operation(summary = "Listar lotes que vencen en los próximos N días")
    public ResponseEntity<List<LoteDTO>> listarPorVencer(
            @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(loteServices.listarPorVencer(dias));
    }
}
