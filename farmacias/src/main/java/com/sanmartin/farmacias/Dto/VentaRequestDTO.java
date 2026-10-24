package com.sanmartin.farmacias.Dto;


import com.sanmartin.farmacias.Entity.MetodoPago;
import com.sanmartin.farmacias.Entity.TipoComprobante;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Datos para registrar una venta")
public record VentaRequestDTO(
        @Schema(description = "ID del cliente (opcional, null si es anónimo)", example = "1")
        Long idCliente,

        @Schema(description = "ID de la caja abierta", example = "1")
        @NotNull(message = "La caja es obligatoria")
        Long idCaja,

        @Schema(description = "ID del usuario que vende", example = "1")
        @NotNull(message = "El usuario es obligatorio")
        Long idUsuario,

        @Schema(description = "Tipo de comprobante", example = "BOLETA")
        @NotNull(message = "El tipo de comprobante es obligatorio")
        TipoComprobante tipoComprobante,

        @Schema(description = "Método de pago", example = "EFECTIVO")
        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodoPago,

        @Schema(description = "Detalles de la venta")
        @NotEmpty(message = "Debe incluir al menos un producto")
        @Valid
        List<DetalleVentaRequestDTO> detalles

) {
}
