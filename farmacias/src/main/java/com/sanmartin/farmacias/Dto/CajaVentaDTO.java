// ===== CajaVentaDTO.java =====
package com.sanmartin.farmacias.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Una venta resumida para la tabla "Ventas de esta caja".
public record CajaVentaDTO(
        String numeroVenta,
        LocalDateTime fechaVenta,
        String cliente,
        String vendedor,
        String tipoComprobante,
        String metodoPago,
        BigDecimal total,
        String estado,       // "EMITIDA" o "ANULADA"
        List<CajaVentaDetalleDTO> detalles
) {}