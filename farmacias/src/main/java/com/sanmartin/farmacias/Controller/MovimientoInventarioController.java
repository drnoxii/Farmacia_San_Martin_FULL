package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.MovimientoInventarioResponseDTO;
import com.sanmartin.farmacias.Services.MovimientoInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/movimientos")
@Tag(
        name = "Kardex",
        description = "Historial de movimientos de inventario"
)
public class MovimientoInventarioController {
    private final MovimientoInventarioService movimientoService;

    public MovimientoInventarioController(
            MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    // ==========================================
    // KARDEX - SOLO ADMIN
    // ==========================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Consultar Kardex",
            description = "Lista los movimientos de inventario con paginación. Solo ADMIN."
    )
    public Page<MovimientoInventarioResponseDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La página no puede ser negativa."
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tamaño debe estar entre 1 y 100."
            );
        }

        return movimientoService.listar(page, size);
    }
}
