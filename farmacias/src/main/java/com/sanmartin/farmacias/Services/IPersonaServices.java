package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.PersonaDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IPersonaServices {
    PersonaDTO crear(PersonaDTO dto);

    PersonaDTO obtenerPorId(Long id);

    List<PersonaDTO> listar();

    PersonaDTO actualizar(Long id, PersonaDTO dto);

    void eliminar(Long id);
}

