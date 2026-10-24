package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.CajaAperturaDTO;
import com.sanmartin.farmacias.Dto.CajaCierreDTO;
import com.sanmartin.farmacias.Dto.CajaDTO;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.ICajaService;
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
@RequestMapping("/api/v1/cajas")
@Tag(name = "Cajas", description = "Apertura y cierre de caja")
public class CajaController {

    private final ICajaService cajaService;

    public CajaController(ICajaService cajaService) {
        this.cajaService = cajaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las cajas")
    public ResponseEntity<List<CajaDTO>> listarTodo() {
        return ResponseEntity.ok(cajaService.listarTodo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener caja por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<CajaDTO> obtener(@PathVariable Long id) {
        return cajaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Caja no encontrada con ID: " + id));
    }

    @GetMapping("/abiertas")
    @Operation(summary = "Listar cajas abiertas")
    public ResponseEntity<List<CajaDTO>> listarAbiertas() {
        return ResponseEntity.ok(cajaService.listarAbiertas());
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Listar cajas de un usuario")
    public ResponseEntity<List<CajaDTO>> listarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(cajaService.listarPorUsuario(idUsuario));
    }

    @PostMapping("/abrir")
    @Operation(summary = "Abrir caja")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Caja abierta"),
            @ApiResponse(responseCode = "400", description = "El usuario ya tiene caja abierta"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<CajaDTO> abrir(@RequestBody @Valid CajaAperturaDTO dto) {
        return new ResponseEntity<>(cajaService.abrir(dto), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar caja indicando el monto contado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Caja cerrada"),
            @ApiResponse(responseCode = "400", description = "La caja ya está cerrada"),
            @ApiResponse(responseCode = "404", description = "Caja no encontrada")
    })
    public ResponseEntity<CajaDTO> cerrar(@PathVariable Long id,
                                          @RequestBody @Valid CajaCierreDTO dto) {
        return ResponseEntity.ok(cajaService.cerrar(id, dto));
    }
}
