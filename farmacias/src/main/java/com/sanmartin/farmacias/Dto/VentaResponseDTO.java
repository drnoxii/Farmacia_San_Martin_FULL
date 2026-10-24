package com.sanmartin.farmacias.Dto;


import com.sanmartin.farmacias.Entity.EstadoVenta;
import com.sanmartin.farmacias.Entity.MetodoPago;
import com.sanmartin.farmacias.Entity.TipoComprobante;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Venta (respuesta)")
public record VentaResponseDTO(

        Long idVenta,
        String numeroVenta,
        Long idCliente,
        String nombreCliente,
        Long idCaja,
        Long idUsuario,
        String nombreUsuario,
        TipoComprobante tipoComprobante,
        MetodoPago metodoPago,
        BigDecimal total,
        EstadoVenta estado,
        LocalDateTime fechaVenta,
        List<DetalleVentaResponseDTO> detalles
) {
}
