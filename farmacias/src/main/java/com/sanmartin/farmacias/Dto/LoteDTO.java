package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.EstadoLote;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Datos del lote")
public record LoteDTO(

        @Schema(description = "ID del lote", example = "1")
        Long idLote,

        @Schema(description = "ID del producto", example = "5")
        Long idProducto,

        @Schema(description = "Nombre del producto", example = "Paracetamol 500mg")
        String nombreProducto,

        @Schema(description = "Número de lote", example = "L2024-05")
        String numeroLote,

        @Schema(description = "Stock del lote", example = "100")
        Integer stockLote,

        @Schema(description = "Fecha de vencimiento", example = "2027-03-31")
        LocalDate fechaVencimiento,

        @Schema(description = "Estado", example = "ACTIVO")
        EstadoLote estado

) {
}
