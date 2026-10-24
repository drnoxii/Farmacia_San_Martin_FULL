package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.EstadoDetalleVenta;
import io.swagger.v3.oas.annotations.Parameter;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DetalleVentaDto(
        @Parameter(hidden = true)
        Long idDetalleVenta,

        Long idProducto,
        String nombreProducto,
        Long idLote,
        String numeroLote,
        LocalDate fechaVencimiento,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        EstadoDetalleVenta estado,
        Integer cantidadDevuelta
) {
}
