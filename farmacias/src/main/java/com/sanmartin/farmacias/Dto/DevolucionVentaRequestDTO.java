package com.sanmartin.farmacias.Dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record DevolucionVentaRequestDTO(
        @NotNull @Positive Long idUsuario,
        @NotEmpty List<@NotNull @Positive Long> idDetalles) {
}
