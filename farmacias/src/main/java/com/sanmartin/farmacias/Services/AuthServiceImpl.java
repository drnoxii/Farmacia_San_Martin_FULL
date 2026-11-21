package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.LoginDTO;
import com.sanmartin.farmacias.Dto.LoginResponseDto;
import com.sanmartin.farmacias.Dto.RegistroDTO;
import com.sanmartin.farmacias.Dto.UsuarioDTO;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Persona;
import com.sanmartin.farmacias.Entity.Usuario;
import com.sanmartin.farmacias.Repository.PersonaRepository;
import com.sanmartin.farmacias.Repository.UsuarioRepository;
import com.sanmartin.farmacias.security.JwtService;
import com.sanmartin.farmacias.security.UsuarioPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PersonaRepository personaRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginDTO dto) {

        // 1. Solicitar a Spring Security que autentique
        Authentication autenticacion;

        try {
            autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.correo().trim(),
                            dto.password()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales incorrectas o usuario inactivo"
            );
        }

        // 2. Obtener el usuario autenticado
        UsuarioPrincipal principal =
                (UsuarioPrincipal) autenticacion.getPrincipal();

        // 3. Recuperar los datos del empleado desde Oracle
        Usuario usuario = usuarioRepository
                .findByCorreo(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no disponible"
                ));

        // 4. Generar JWT solamente después de autenticar
        String token = jwtService.generarToken(principal);

        // 5. Devolver token y datos del empleado
        return new LoginResponseDto(
                token,
                toDto(usuario)
        );
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
