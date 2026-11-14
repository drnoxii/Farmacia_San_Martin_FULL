// ===== CajaResumenDTO.java =====
package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.EstadoCaja;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Todo lo que la pantalla "Gestión de Caja" necesita en una sola respuesta.
// Los nombres de los campos deben coincidir EXACTO con la interfaz CajaResumen de Angular.
public record CajaResumenDTO(
        Long idCaja,
        String nombreUsuario,
        EstadoCaja estado,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        BigDecimal montoInicial,
        BigDecimal ventasEfectivo,
        BigDecimal ventasYape,
        BigDecimal ventasPlin,
        BigDecimal ventasTarjeta,
        BigDecimal totalVendido,
        long cantidadVentas,
        BigDecimal montoEsperadoEfectivo,
        BigDecimal montoFinal,
        BigDecimal diferencia,
        List<CajaVentaDTO> ventas
) {}