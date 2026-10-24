package com.sanmartin.farmacias.Dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LaboratorioDto(

        @Parameter(hidden = true)
        Long idLaboratorio,

        @NotBlank(message = "El nombre del laboratorio no puede estar vacío")
        @Size(max = 50, message = "El nombre del laboratorio no puede superar los 50 caracteres")
        String nombreLaboratorio,

        @NotBlank(message = "El país de origen no puede estar vacío")
        @Size(max = 50, message = "El país de origen no puede superar los 50 caracteres")
        String paisOrigenLab
) {
}