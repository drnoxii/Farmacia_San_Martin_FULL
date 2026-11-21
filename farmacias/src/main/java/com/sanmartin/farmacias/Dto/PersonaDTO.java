package com.sanmartin.farmacias.Dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sanmartin.farmacias.Entity.TipoDocumento;
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

        @Schema(description = "Nombre", example = "Juan Carlos")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @Schema(description = "Teléfono", example = "999888777")
        @Size(max = 15)
        String telefono,

        @Schema(description = "Dirección", example = "Av. Lima 123")
        @Size(max = 250)
        String direccion,

        @Schema(description = "Apellido paterno")
        String apellidoPaterno,

        @Schema(description = "Apellido materno")
        String apellidoMaterno,

        @Schema(description = "Tipo de documento")
        TipoDocumento tipoDocumento,

        @Schema(
                description = "Sexo: MASCULINO o FEMENINO",
                example = "MASCULINO"
        )
        String sexo


) {


    public PersonaDTO(
            Long idPersona,
            String numeroDocumento,
            String nombre,
            String telefono,
            String direccion,
            String apellidoPaterno,
            String apellidoMaterno,
            TipoDocumento tipoDocumento) {

        this(
                idPersona,
                numeroDocumento,
                nombre,
                telefono,
                direccion,
                apellidoPaterno,
                apellidoMaterno,
                tipoDocumento,
                null
        );
    }

    // DTO para consultar DNI
    public record ConsultaDni(

            @Schema(description = "DNI", example = "12345678")
            @JsonAlias("dni")
            @NotBlank(message = "El DNI es obligatorio")
            @Pattern(regexp = "\\d{8}",
                    message = "El DNI debe tener 8 dígitos")
            String numeroDocumento

    ) {}

    // Respuesta de Factiliza
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FactilizaResponse(

            boolean success,
            FactilizaData data,
            String message

    ) {}

    // Datos recibidos de Factiliza
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FactilizaData(

            @JsonProperty("numero")
            @JsonAlias({"dni", "number"})
            String dni,

            @JsonProperty("nombres")
            @JsonAlias({"name", "first_name"})
            String nombres,

            @JsonProperty("apellido_paterno")
            @JsonAlias({"apellidoPaterno", "paterno"})
            String apellidoPaterno,

            @JsonProperty("apellido_materno")
            @JsonAlias({"apellidoMaterno", "materno"})
            String apellidoMaterno,

            @JsonProperty("direccion")
            String direccion,

            @JsonProperty("direccion_completa")
            String direccionCompleta,

            @JsonProperty("sexo")
            String sexo

    ) {}

}




