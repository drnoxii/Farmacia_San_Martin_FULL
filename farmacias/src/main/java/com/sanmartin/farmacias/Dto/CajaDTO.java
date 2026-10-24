package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.EstadoCaja;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Datos de la caja")
public record CajaDTO(

        Long idCaja,
        Long idUsuario,
        String nombreUsuario,
        BigDecimal montoInicial,
        BigDecimal montoFinal,
        BigDecimal diferencia,
        EstadoCaja estado,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre
) {
}
