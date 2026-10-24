package com.sanmartin.farmacias.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de inicio de sesión")
public record LoginDTO(

        @Schema(description = "Correo del usuario", example = "juan@farmacia.com")
        @NotBlank(message = "El correo es obligatorio")
        String correo,

        @Schema(description = "Contraseña", example = "secreto123")
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
