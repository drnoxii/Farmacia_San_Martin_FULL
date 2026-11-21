package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.VentaRequestDTO;
import com.sanmartin.farmacias.Dto.VentaResponseDTO;
import com.sanmartin.farmacias.Dto.DevolucionVentaRequestDTO;
import com.sanmartin.farmacias.Dto.DevolucionVentaResponseDTO;
import com.sanmartin.farmacias.Services.DevolucionVentaService;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.IVentaService;
import com.sanmartin.farmacias.security.UsuarioPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
@Tag(name = "Ventas", description = "Registro y consulta de ventas")
@SecurityRequirement(name = "BearerAuth")
public class VentaController {

    private final IVentaService ventaServices;
    private final DevolucionVentaService devoluciones;

    public VentaController(IVentaService ventaServices, DevolucionVentaService devoluciones) {
        this.ventaServices = ventaServices;
        this.devoluciones = devoluciones;
    }

    @GetMapping
    @Operation(summary = "Listar todas las ventas")
    public ResponseEntity<List<VentaResponseDTO>> listarTodo() {
        return ResponseEntity.ok(ventaServices.listarTodo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener venta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<VentaResponseDTO> obtener(@PathVariable Long id) {
        return ventaServices.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con ID: " + id));
    }

    @PostMapping
    @Operation(summary = "Registrar venta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
            @ApiResponse(responseCode = "404", description = "Caja, usuario, cliente o lote no encontrados")
    })
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@accesoCaja.esPropietario(#dto.idCaja(), authentication)"
    )
    public ResponseEntity<VentaResponseDTO> registrar(
            @RequestBody @Valid VentaRequestDTO dto,
            @AuthenticationPrincipal UsuarioPrincipal principal) {

        Long idUsuario = principal.usuario().getIdUsuario();

        return new ResponseEntity<>(
                ventaServices.registrar(dto, idUsuario),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/{id}/devoluciones")
    @Operation(summary = "Anular detalles de venta y devolver sus cantidades al lote original")
    public ResponseEntity<DevolucionVentaResponseDTO> devolver(@PathVariable Long id,
            @RequestBody @Valid DevolucionVentaRequestDTO dto) {
        return ResponseEntity.ok(devoluciones.devolver(id, dto));
    }

    public ResponseEntity<VentaResponseDTO> obtenerComprobante(@PathVariable Long id){
        return ventaServices.buscarPorId(id).map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la venta con ID: "+ id));
    }

}
