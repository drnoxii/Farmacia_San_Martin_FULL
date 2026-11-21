package com.sanmartin.farmacias.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Datos para abrir una caja")
public record CajaAperturaDTO(

        @Schema(description = "Monto inicial", example = "200.00")
        @NotNull(message = "El monto inicial es obligatorio")
        @DecimalMin(
                value = "0.0",
                message = "El monto no puede ser negativo"
        )
        BigDecimal montoInicial

) {
}
