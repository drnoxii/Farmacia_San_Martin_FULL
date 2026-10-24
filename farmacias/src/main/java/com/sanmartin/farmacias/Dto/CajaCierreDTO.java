package com.sanmartin.farmacias.Dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Datos para cerrar una caja")
public record CajaCierreDTO(

        @Schema(description = "ID del usuario en sesión", example = "1")
        @NotNull(message = "El usuario es obligatorio")
        Long idUsuario,

        @Schema(description = "Monto contado al cierre", example = "450.50")
        @NotNull(message = "El monto contado es obligatorio")
        @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
        BigDecimal montoContado

) {
}
