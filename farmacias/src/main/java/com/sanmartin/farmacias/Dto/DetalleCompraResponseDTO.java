package com.sanmartin.farmacias.Dto;


import com.sanmartin.farmacias.Entity.EstadoLote;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Detalle de compra (respuesta)")
public record DetalleCompraResponseDTO(

        Long idDetalleCompra,
        Long idProducto,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioCosto,
        BigDecimal subtotal,
        String numeroLote,
        LocalDate fechaVencimiento,
        EstadoLote estadoLote
) {
}
