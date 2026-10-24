package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.PersonaDTO;
import com.sanmartin.farmacias.Entity.Persona;
import com.sanmartin.farmacias.Repository.PersonaRepository;
import com.sanmartin.farmacias.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonaServiceImpl implements IPersonaServices {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;

    public PersonaServiceImpl(PersonaRepository personaRepository,
                              UsuarioRepository usuarioRepository) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public PersonaDTO crear(PersonaDTO dto) {

        if (personaRepository.existsByNumeroDocumento(dto.numeroDocumento())) {
            throw new RuntimeException("Ya existe una persona con ese DNI");
        }

        Persona persona = new Persona();
        persona.setNumeroDocumento(dto.numeroDocumento());
        persona.setNombre(dto.nombre());
        persona.setTelefono(dto.telefono());
        persona.setDireccion(dto.direccion());

        persona = personaRepository.save(persona);
        return toDTO(persona);
    }

    @Override
    public PersonaDTO obtenerPorId(Long id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
        return toDTO(persona);
    }


    @Override
    public List<PersonaDTO> listar() {
        return personaRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }


    @Override
    @Transactional
    public PersonaDTO actualizar(Long id, PersonaDTO dto) {

        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

        if (!persona.getNumeroDocumento().equals(dto.numeroDocumento())
                && personaRepository.existsByNumeroDocumento(dto.numeroDocumento())) {
            throw new RuntimeException("Ya existe otra persona con ese DNI");
        }

        persona.setNumeroDocumento(dto.numeroDocumento());
        persona.setNombre(dto.nombre());
        persona.setTelefono(dto.telefono());
        persona.setDireccion(dto.direccion());

        persona = personaRepository.save(persona);
        return toDTO(persona);
    }


    @Override
    @Transactional
    public void eliminar(Long id) {

        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

        boolean tieneUsuario = usuarioRepository.findAll().stream()
                .anyMatch(u -> u.getPersona() != null
                        && u.getPersona().getIdPersona().equals(id));

        if (tieneUsuario) {
            throw new RuntimeException("No se puede eliminar: la persona tiene un usuario asociado");
        }

        personaRepository.delete(persona);
    }


    private PersonaDTO toDTO(Persona p) {
        return new PersonaDTO(
                p.getIdPersona(),
                p.getNumeroDocumento(),
                p.getNombre(),
                p.getTelefono(),
                p.getDireccion()
        );
    }
}
