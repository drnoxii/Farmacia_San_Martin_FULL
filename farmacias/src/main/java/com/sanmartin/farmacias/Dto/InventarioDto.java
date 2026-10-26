package com.sanmartin.farmacias.Dto;

import java.math.BigDecimal;

public record InventarioDto(
        Long idProducto,
        String nombreProducto,
        BigDecimal precioVenta,
        Integer stockMinimo,
        Long stockTotal
) {
}
