package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.LoginDTO;
import com.sanmartin.farmacias.Dto.RegistroDTO;
import com.sanmartin.farmacias.Dto.UsuarioDTO;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Persona;
import com.sanmartin.farmacias.Entity.Usuario;
import com.sanmartin.farmacias.Repository.PersonaRepository;
import com.sanmartin.farmacias.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PersonaRepository personaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioDTO registrar(RegistroDTO dto) {
        if (personaRepository.existsByNumeroDocumento(dto.numeroDocumento())) {
            throw new RuntimeException("Ya existe una persona con ese DNI");
        }
        if (usuarioRepository.existsByCorreo(dto.correo())) {
            throw new RuntimeException("Ya existe un usuario con ese nombre de usuario");
        }

        //  Persona
        Persona persona = new Persona();
        persona.setNumeroDocumento(dto.numeroDocumento());
        persona.setNombre(dto.nombre());
        persona.setTelefono(dto.telefono());
        persona.setDireccion(dto.direccion());
        persona = personaRepository.save(persona);

        //  Usuario
        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setCorreo(dto.correo());
        usuario.setPassword(passwordEncoder.encode(dto.password()));
        usuario.setRol(dto.rol());
        usuario.setEstadoGeneral(EstadoGeneral.ACTIVO);
        usuario = usuarioRepository.save(usuario);

        return toDto(usuario);
    }

    @Override
    public UsuarioDTO login(LoginDTO dto) {
        Usuario usuario = usuarioRepository.findByCorreo(dto.correo())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.password(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        if (usuario.getEstadoGeneral() == EstadoGeneral.INACTIVO) {
            throw new RuntimeException("Usuario inactivo");
        }

        return toDto(usuario);
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
