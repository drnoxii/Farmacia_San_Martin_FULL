package com.sanmartin.farmacias.Dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaDto(
        @Parameter(hidden = true)
        Long idCategoria,

        @NotBlank(message = "Este campo es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String nombreCategoria,

        @NotBlank(message = "Este campo es obligatorio")
        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String descripCategoria

) {
}
