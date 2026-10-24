package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.DetalleVenta;
import com.sanmartin.farmacias.Entity.EstadoVenta;
import com.sanmartin.farmacias.Entity.TipoComprobante;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaDto(
        @Parameter(hidden = true)
        Long idVenta,

        @NotNull(message = "El cliente es obligatoria")
        Long idCliente,

        @NotNull(message = "La caja es obligatoria")
        Long idCaja,

        @NotNull(message = "El usuario es obligatorio")
        Long idUsuario,

        String numeroVenta,

        @Schema(description = "Tipo de comprobante", example = "BOLETA")
        @NotNull(message = "El tipo de comprobante es obligatorio")
        TipoComprobante tipoComprobante,

        @Schema(description = "Método de pago", example = "EFECTIVO")
        @NotNull(message = "El método de pago es obligatorio")
        String metodoPago,

        @DecimalMin(value = "0.0")
        BigDecimal subtotal,

        @DecimalMin(value = "0.0")
        BigDecimal total,

        EstadoVenta estado,
        LocalDateTime fechaVenta,

        @Schema(description = "Detalles de la venta")
        @NotEmpty(message = "Debe incluir al menos un producto")
        @Valid
        List<DetalleVenta> detalleVentas
) {
}
