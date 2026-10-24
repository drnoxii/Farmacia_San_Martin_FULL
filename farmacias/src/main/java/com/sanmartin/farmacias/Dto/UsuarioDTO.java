package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Rol;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Schema(description = "Datos del usuario")
public record UsuarioDTO(

        @Schema(description = "ID del usuario", example = "1")
        @Parameter(hidden = true)
        Long idUsuario,

        @Schema(description = "ID de la persona", example = "1")
        Long idPersona,

        @Schema(description = "DNI", example = "12345678")
        String numeroDocumento,

        @Schema(description = "Nombre completo", example = "Juan Pérez")
        String nombre,

        @Schema(description = "Teléfono", example = "999888777")
        String telefono,

        @Schema(description = "Dirección", example = "Av. Lima 123")
        String direccion,

        @Schema(description = "Correo", example = "juan@farmacia.com")
        String correo,

        @Schema(description = "Rol", example = "AUXILIAR")
        Rol rol,

        @Schema(description = "Estado", example = "ACTIVO")
        EstadoGeneral estado

) {
}
