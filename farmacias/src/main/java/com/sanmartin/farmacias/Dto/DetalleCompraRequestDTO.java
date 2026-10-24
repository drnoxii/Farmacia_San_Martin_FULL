package com.sanmartin.farmacias.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Línea de detalle de una compra")
public record DetalleCompraRequestDTO(

        @Schema(description = "ID del producto", example = "5")
        @NotNull(message = "El producto es obligatorio")
        Long idProducto,

        @Schema(description = "Cantidad comprada", example = "100")
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        Integer cantidad,

        @Schema(description = "Precio de costo unitario", example = "0.50")
        @NotNull(message = "El precio de costo es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        BigDecimal precioCosto,

        @Schema(description = "Número de lote", example = "L2024-05")
        @NotBlank(message = "El número de lote es obligatorio")
        @Size(max = 20)
        String numeroLote,

        @Schema(description = "Fecha de vencimiento", example = "2027-03-31")
        @NotNull(message = "La fecha de vencimiento es obligatoria")
        @Future(message = "La fecha de vencimiento debe ser futura")
        LocalDate fechaVencimiento


) {
}
