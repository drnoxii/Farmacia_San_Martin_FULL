package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.ProveedorDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface IProveedorServices {
    List<ProveedorDto> listarTodo();
    Optional<ProveedorDto> buscarPorId(Long id);
    ProveedorDto registrar(ProveedorDto pr);
    Optional<ProveedorDto> actualizar(Long id, ProveedorDto pr);
    boolean eliminar(Long id);
}
