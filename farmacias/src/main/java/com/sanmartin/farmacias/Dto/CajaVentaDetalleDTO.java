// ===== CajaVentaDetalleDTO.java =====
package com.sanmartin.farmacias.Dto;

import java.math.BigDecimal;

// Una línea de producto dentro de una venta (lo que se muestra al expandir la venta).
public record CajaVentaDetalleDTO(
        String producto,
        String numeroLote,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String estado        // "V" = vendido, "D" = devuelto
) {}