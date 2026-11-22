package com.sanmartin.farmacias.Dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record DevolucionVentaRequestDTO(
        @NotEmpty(message = "Selecciona al menos un detalle")
        List<@NotNull @Positive Long> idDetalles,

        @NotBlank(message = "El motivo de devolución es obligatorio")
        @Size(
                max = 500,
                message = "La observación no puede superar 500 caracteres"
        )
        String observacion

) {}
