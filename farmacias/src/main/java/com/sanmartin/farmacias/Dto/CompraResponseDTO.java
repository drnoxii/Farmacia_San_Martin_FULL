package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.EstadoCompra;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Compra (respuesta)")
public record CompraResponseDTO(
        Long idCompra,
        Long idProveedor,
        String razonSocialProveedor,
        Long idUsuario,
        String nombreUsuario,
        LocalDateTime fechaCompra,
        BigDecimal totalCompra,
        EstadoCompra estado,
        List<DetalleCompraResponseDTO> detalles


) {
}
