package com.sanmartin.farmacias.Dto;


import com.sanmartin.farmacias.Entity.EstadoDetalleVenta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Detalle de venta (respuesta)")
public record DetalleVentaResponseDTO(

        Long idDetalleVenta,
        Long idLote,
        String numeroLote,
        LocalDate fechaVencimiento,
        Long idProducto,
        String nombreProducto,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        EstadoDetalleVenta estado

) {
}
