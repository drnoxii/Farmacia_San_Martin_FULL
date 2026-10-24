package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.EstadoGeneral;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductoDto(
        @Parameter(hidden = true)
        Long idProducto,

        @NotBlank(message = "El nombre del producto es obligatoria")
        @Size(max = 100, message = "El nombre debe maximo 100 caracteres")
        String nombreProducto,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 255, message = "La descripción no debe pasar de los 255 caracteres")
        String descripcion,

        @NotNull(message = "La Categoria es obligatoria")
        Long idCategoria,

        @NotNull(message = "El Laboratorio es obligatorio")
        Long idLaboratorio,

        @NotNull(message = "El precio de compra es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
        BigDecimal precioCompra,

        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
        BigDecimal precioVenta,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El Stock no puede ser negativo")
        Integer stockMinimo,

        @NotNull(message = "Este campo es obligatorio")
        EstadoGeneral estadoGeneral
) {
}
