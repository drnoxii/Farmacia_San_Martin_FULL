package com.sanmartin.farmacias.Dto;

public record LoginResponseDto(
        String token,
        UsuarioDTO usuario
) {
}
