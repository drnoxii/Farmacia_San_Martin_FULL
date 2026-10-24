package com.sanmartin.farmacias.Services;


import com.sanmartin.farmacias.Dto.UsuarioDTO;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Persona;
import com.sanmartin.farmacias.Entity.Rol;
import com.sanmartin.farmacias.Entity.Usuario;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.PersonaRepository;
import com.sanmartin.farmacias.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements IUsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PersonaRepository personaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return toDto(u);
    }

    @Override
    public List<UsuarioDTO> listar() {
        return usuarioRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<UsuarioDTO> listarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol).stream().map(this::toDto).toList();
    }

    @Override
    public List<UsuarioDTO> listarPorEstado(EstadoGeneral estado) {
        return usuarioRepository.findByEstadoGeneral(estado).stream().map(this::toDto).toList();
    }

    @Override
    public UsuarioDTO cambiarEstado(Long id, EstadoGeneral estado) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setEstadoGeneral(estado);         // ← cambio
        return toDto(usuarioRepository.save(u));
    }

    @Override
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);

    }

    private UsuarioDTO toDto(Usuario u) {
        Persona p = u.getPersona();
        return new UsuarioDTO(
                u.getIdUsuario(),
                p.getIdPersona(),
                p.getNumeroDocumento(),
                p.getNombre(),
                p.getTelefono(),
                p.getDireccion(),
                u.getCorreo(),
                u.getRol(),
                u.getEstadoGeneral()
        );
    }
}
