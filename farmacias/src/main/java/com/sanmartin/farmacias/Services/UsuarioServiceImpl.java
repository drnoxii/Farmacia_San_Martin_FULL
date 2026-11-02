
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PersonaRepository personaRepository) {

        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
    }

    // OBTENER USUARIO POR ID
    @Override
    public UsuarioDTO obtenerPorId(Long id) {

        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado"));

        return toDto(u);
    }

    // LISTAR USUARIOS
    @Override
    public List<UsuarioDTO> listar() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // LISTAR POR ROL
    @Override
    public List<UsuarioDTO> listarPorRol(Rol rol) {

        return usuarioRepository.findByRol(rol)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // LISTAR POR ESTADO
    @Override
    public List<UsuarioDTO> listarPorEstado(EstadoGeneral estado) {

        return usuarioRepository.findByEstadoGeneral(estado)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // CAMBIAR ESTADO
    @Override
    public UsuarioDTO cambiarEstado(Long id, EstadoGeneral estado) {

        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado"));

        u.setEstadoGeneral(estado);

        return toDto(usuarioRepository.save(u));
    }

    // ACTUALIZAR DATOS DEL USUARIO
    @Override
    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioDTO datos) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado"));

        Persona persona = usuario.getPersona();

        // Actualizar datos personales
        persona.setNombre(datos.nombre());
        persona.setNumeroDocumento(datos.numeroDocumento());
        persona.setTelefono(datos.telefono());
        persona.setDireccion(datos.direccion());

        // Guardar persona
        personaRepository.save(persona);

        // Actualizar datos de la cuenta
        usuario.setCorreo(datos.correo());
        usuario.setRol(datos.rol());

        // Guardar usuario
        Usuario actualizado = usuarioRepository.save(usuario);

        return toDto(actualizado);
    }

    // ELIMINAR USUARIO
    @Override
    public void eliminar(Long id) {

        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        usuarioRepository.deleteById(id);
    }

    // CONVERTIR A DTO
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
