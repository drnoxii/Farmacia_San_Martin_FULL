package com.sanmartin.farmacias.Services;


import com.sanmartin.farmacias.Dto.UsuarioDTO;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Persona;
import com.sanmartin.farmacias.Entity.Rol;
import com.sanmartin.farmacias.Entity.Usuario;
import com.sanmartin.farmacias.Exception.DuplicateResourceException;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.PersonaRepository;
import com.sanmartin.farmacias.Repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioDTO dto) {

        // 1. Buscar el usuario
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        Persona persona = usuario.getPersona();

        // 2. Validar datos obligatorios
        String dni = dto.numeroDocumento() == null
                ? "" : dto.numeroDocumento().trim();

        String nombre = dto.nombre() == null
                ? "" : dto.nombre().trim();

        String correo = dto.correo() == null
                ? "" : dto.correo().trim();

        if (!dni.matches("\\d{8}")
                || nombre.isBlank()
                || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verifica el DNI, nombre y correo"
            );
        }

        // 3. Evitar DNI duplicado en otra persona
        personaRepository.findByNumeroDocumento(dni)
                .ifPresent(existente -> {
                    if (!existente.getIdPersona()
                            .equals(persona.getIdPersona())) {

                        throw new DuplicateResourceException(
                                "El DNI ya pertenece a otra persona"
                        );
                    }
                });

        // 4. Evitar correo duplicado en otro usuario
        usuarioRepository.findByCorreo(correo)
                .ifPresent(existente -> {
                    if (!existente.getIdUsuario()
                            .equals(usuario.getIdUsuario())) {

                        throw new DuplicateResourceException(
                                "El correo ya pertenece a otro usuario"
                        );
                    }
                });

        // 5. Actualizar datos personales
        persona.setNumeroDocumento(dni);
        persona.setNombre(nombre);
        persona.setTelefono(dto.telefono());
        persona.setDireccion(dto.direccion());

        // 6. Actualizar correo del usuario
        usuario.setCorreo(correo);

        // No modificar contraseña, rol ni estado

        personaRepository.save(persona);
        usuarioRepository.save(usuario);

        return toDto(usuario);
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
