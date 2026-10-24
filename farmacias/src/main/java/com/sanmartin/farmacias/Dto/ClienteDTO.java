package com.sanmartin.farmacias.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos del cliente")
public record ClienteDTO(
        @Schema(description = "ID del cliente", example = "1")
        Long idCliente,

        @Schema(description = "DNI o RUC", example = "12345678")
        @NotBlank(message = "El documento es obligatorio")
        @Size(max = 11)
        String numeroDocumento,

        @Schema(description = "Nombre completo o razón social", example = "Juan Pérez")
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
