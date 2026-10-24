package com.sanmartin.farmacias.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos de la persona")
public record PersonaDTO(

        @Schema(description = "ID de la persona", example = "1")
        Long idPersona,

        @Schema(description = "DNI", example = "12345678")
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
        String numeroDocumento,

        @Schema(description = "Nombre completo", example = "Juan Pérez")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @Schema(description = "Teléfono", example = "999888777")
        @Size(max = 15)
        String telefono,

        @Schema(description = "Dirección", example = "Av. Lima 123")
        @Size(max = 250)
        String direccion
) {
}
