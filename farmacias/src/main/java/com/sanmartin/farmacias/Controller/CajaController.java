package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.CajaAperturaDTO;
import com.sanmartin.farmacias.Dto.CajaCierreDTO;
import com.sanmartin.farmacias.Dto.CajaDTO;
import com.sanmartin.farmacias.Dto.CajaResumenDTO;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.ICajaService;
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
@RequestMapping("/api/v1/cajas")
@Tag(name = "Cajas", description = "Apertura y cierre de caja")
@SecurityRequirement(name = "BearerAuth")
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
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@accesoCaja.esPropietario(#id, authentication)"
    )
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
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<CajaDTO> abrir(
            @RequestBody @Valid CajaAperturaDTO dto,
            @AuthenticationPrincipal UsuarioPrincipal principal) {

        Long idUsuario = principal.usuario().getIdUsuario();

        return new ResponseEntity<>(
                cajaService.abrir(dto, idUsuario),
                HttpStatus.CREATED
        );
    }


    @PostMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar caja indicando los montos contados")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@accesoCaja.esPropietario(#id, authentication)"
    )
    public ResponseEntity<CajaDTO> cerrar(
            @PathVariable Long id,
            @RequestBody @Valid CajaCierreDTO dto,
            @AuthenticationPrincipal UsuarioPrincipal principal) {

        // Usuario obtenido del JWT
        Long idUsuario = principal.usuario().getIdUsuario();

        // Comprobar si es administrador
        boolean esAdmin = principal.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN")
                );

        return ResponseEntity.ok(
                cajaService.cerrar(id, dto, idUsuario, esAdmin)
        );
    }

    @GetMapping("/usuario/{idUsuario}/abierta")
    @Operation(summary = "Obtener la caja abierta de un usuario")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "#idUsuario == authentication.principal.usuario().getIdUsuario()"
    )
    public ResponseEntity<CajaDTO> obtenerCajaAbierta(@PathVariable Long idUsuario) {
        return cajaService.buscarCajaAbiertaPorUsuario(idUsuario)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no tiene ninguna caja abierta"));
    }

    /**
     * QUÉ HACE: devuelve el resumen (totales + ventas) de una caja.
     * POR QUÉ:  es el endpoint que usa la pantalla de caja para mostrar
     *           cuánto dinero ha ingresado.
     */
    @GetMapping("/{id}/resumen")
    @Operation(summary = "Resumen de caja con totales y ventas")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@accesoCaja.esPropietario(#id, authentication)"
    )
    public ResponseEntity<CajaResumenDTO> resumen(@PathVariable Long id) {
        return ResponseEntity.ok(cajaService.resumen(id));
    }
}
