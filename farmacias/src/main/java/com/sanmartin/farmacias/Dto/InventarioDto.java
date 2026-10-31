package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.EstadoGeneral;

import java.math.BigDecimal;

public record InventarioDto(
        Long idProducto,
        String codigo,
        String nombreProducto,
        String descripcion,
        Long idCategoria,
        String nombreCategoria,
        Long idLaboratorio,
        String nombreLaboratorio,
        BigDecimal precioCompra,
        BigDecimal precioVenta,
        Integer stockMinimo,
        EstadoGeneral estadoGeneral,
        Long stockTotal
) {
}
