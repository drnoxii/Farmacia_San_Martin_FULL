package com.sanmartin.farmacias.Dto;

import com.sanmartin.farmacias.Entity.EstadoGeneral;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos del proveedor")
public record ProveedorDto(
        @Schema(description = "ID del proveedor", example = "1")
        Long idProveedor,

        @Schema(description = "Razón social", example = "Distribuidora Farma SAC")
        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 150)
        String razonSocial,

        @Schema(description = "RUC (11 dígitos)", example = "20123456789")
        @NotBlank(message = "El RUC es obligatorio")
        @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
        String ruc,

        @Schema(description = "Teléfono", example = "999888777")
        @Size(max = 15)
        String telefono,

        @Schema(description = "Correo", example = "ventas@farma.com")
        @Email(message = "Correo inválido")
        @Size(max = 100)
        String correo,

        @Schema(description = "Dirección", example = "Av. Lima 456")
        @Size(max = 250)
        String direccion,

        @Schema(description = "Estado", example = "ACTIVO")
        EstadoGeneral estado
) {
}
