package com.sanmartin.farmacias.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoInventarioResponseDTO(

        Long idMovimiento,

        // Información del producto
        Long idProducto,
        String codigoProducto,
        String nombreProducto,

        // Información del lote
        Long idLote,
        String numeroLote,

        // Movimiento
        String tipoMovimiento,
        BigDecimal cantidad,
        BigDecimal stockAnterior,
        BigDecimal stockNuevo,

        // Referencia
        String referenciaTipo,
        Long referenciaId,

        // Responsable
        Long idUsuario,
        String nombreUsuario,

        // Auditoría
        LocalDateTime fecha,
        String observacion
) {
}
