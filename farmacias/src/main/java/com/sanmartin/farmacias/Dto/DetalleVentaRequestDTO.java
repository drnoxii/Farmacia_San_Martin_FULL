package com.sanmartin.farmacias.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Línea de detalle de una venta")
public record DetalleVentaRequestDTO(

        @Schema(description = "ID del lote del que sale el producto", example = "1")
        @NotNull(message = "El lote es obligatorio")
        Long idLote,

        @Schema(description = "Cantidad a vender", example = "3")
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        BigDecimal cantidad

) {
}
