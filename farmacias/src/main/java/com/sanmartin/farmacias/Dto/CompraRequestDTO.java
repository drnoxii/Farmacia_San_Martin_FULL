package com.sanmartin.farmacias.Dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Datos para registrar una compra")
public record CompraRequestDTO(

        @Schema(description = "ID del proveedor", example = "1")
        @NotNull(message = "El proveedor es obligatorio")
        Long idProveedor,

        @Schema(description = "ID del usuario que registra", example = "1")
        @NotNull(message = "El usuario es obligatorio")
        Long idUsuario,

        @Schema(description = "Detalles de la compra")
        @NotEmpty(message = "Debe incluir al menos un producto")
        @Valid
        List<DetalleCompraRequestDTO> detalles

) {
}
