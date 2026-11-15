package com.sanmartin.farmacias.Dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Datos para cerrar una caja")
public record CajaCierreDTO(

        @NotNull(message = "El usuario es obligatorio")
        Long idUsuario,

        @NotNull(message = "El efectivo contado es obligatorio")
        @DecimalMin(value = "0.0", message = "El efectivo no puede ser negativo")
        @Digits(integer = 8, fraction = 2)
        BigDecimal montoContado,

        @NotNull(message = "El monto digital verificado es obligatorio")
        @DecimalMin(value = "0.0", message = "El monto digital no puede ser negativo")
        @Digits(integer = 8, fraction = 2)
        BigDecimal montoDigitalVerificado,

        @Schema(description = "Motivo de las diferencias encontradas")
        String observacionCierre

) {
}
