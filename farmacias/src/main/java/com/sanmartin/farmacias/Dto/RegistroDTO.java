package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para registrar un nuevo usuario (crea Persona + Usuario)")
public record RegistroDTO(

        @Schema(description = "DNI de la persona", example = "12345678")
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
        String direccion,

        @Schema(description = "Correo Electronico", example = "jperez")
        @NotBlank(message = "El correo es obligatorio")
        @Size(max = 50)
        String correo,

        @Schema(description = "Contraseña", example = "secreto123")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100)
        String password,

        @Schema(description = "Rol del usuario", example = "CAJERO")
        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
