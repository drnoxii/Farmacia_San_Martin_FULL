
package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.PersonaDTO;
import com.sanmartin.farmacias.Entity.Persona;
import com.sanmartin.farmacias.Entity.TipoDocumento;
import com.sanmartin.farmacias.Repository.PersonaRepository;
import com.sanmartin.farmacias.Repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class PersonaServiceImpl implements IPersonaServices {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestClient restClient;

    @Value("${reniec.api.url}")
    private String reniecUrl;

    @Value("${reniec.api.token}")
    private String reniecToken;

    public PersonaServiceImpl(
            PersonaRepository personaRepository,
            UsuarioRepository usuarioRepository) {

        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.restClient = RestClient.create();
    }

    // CREAR PERSONA

    @Override
    @Transactional
    public PersonaDTO crear(PersonaDTO dto) {

        validarDni(dto.numeroDocumento());
        validarNombre(dto.nombre());
        validarTipoDocumento(dto.tipoDocumento());

        if (personaRepository.existsByNumeroDocumento(
                dto.numeroDocumento())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una persona con ese DNI"
            );
        }

        Persona persona = new Persona();

        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento(dto.numeroDocumento());
        persona.setNombre(dto.nombre().trim());

        persona.setApellidoPaterno(
                textoOpcional(dto.apellidoPaterno()));

        persona.setApellidoMaterno(
                textoOpcional(dto.apellidoMaterno()));

        persona.setTelefono(
                textoOpcional(dto.telefono()));

        persona.setDireccion(
                textoOpcional(dto.direccion()));

        persona.setSexo(
                normalizarSexo(dto.sexo(), true));

        return toDTO(personaRepository.save(persona));
    }

    // OBTENER PERSONA POR ID


    @Override
    @Transactional(readOnly = true)
    public PersonaDTO obtenerPorId(Long id) {

        return toDTO(buscarPersona(id));
    }


    // LISTAR PERSONAS


    @Override
    @Transactional(readOnly = true)
    public List<PersonaDTO> listar() {

        return personaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ACTUALIZAR PERSONA


    @Override
    @Transactional
    public PersonaDTO actualizar(Long id, PersonaDTO dto) {

        validarDni(dto.numeroDocumento());
        validarNombre(dto.nombre());
        validarTipoDocumento(dto.tipoDocumento());

        Persona persona = buscarPersona(id);

        if (!persona.getNumeroDocumento()
                .equals(dto.numeroDocumento())
                && personaRepository.existsByNumeroDocumento(
                dto.numeroDocumento())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra persona con ese DNI"
            );
        }

        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento(dto.numeroDocumento());
        persona.setNombre(dto.nombre().trim());

        // Los campos opcionales que no se envían
        // conservan su valor anterior.

        if (dto.apellidoPaterno() != null) {
            persona.setApellidoPaterno(
                    textoOpcional(dto.apellidoPaterno()));
        }

        if (dto.apellidoMaterno() != null) {
            persona.setApellidoMaterno(
                    textoOpcional(dto.apellidoMaterno()));
        }

        if (dto.telefono() != null) {
            persona.setTelefono(
                    textoOpcional(dto.telefono()));
        }

        if (dto.direccion() != null) {
            persona.setDireccion(
                    textoOpcional(dto.direccion()));
        }

        if (dto.sexo() != null) {
            persona.setSexo(
                    normalizarSexo(dto.sexo(), true));
        }

        return toDTO(personaRepository.save(persona));
    }


    // ELIMINAR PERSONA


    @Override
    @Transactional
    public void eliminar(Long id) {

        Persona persona = buscarPersona(id);

        boolean tieneUsuario = usuarioRepository.findAll()
                .stream()
                .anyMatch(u ->
                        u.getPersona() != null
                                && id.equals(
                                u.getPersona().getIdPersona())
                );

        if (tieneUsuario) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La persona tiene un usuario asociado"
            );
        }

        personaRepository.delete(persona);
    }


    // CONSULTAR DNI: ORACLE O FACTILIZA


    @Override
    @Transactional
    public PersonaDTO obtenerOPersistirPorDni(String dni) {

        validarDni(dni);

        return personaRepository.findByNumeroDocumento(dni)
                .map(persona -> {

                    if (persona.getTipoDocumento() != null
                            && persona.getTipoDocumento()
                            != TipoDocumento.DNI) {

                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El número pertenece a otro tipo de documento"
                        );
                    }

                    persona.setTipoDocumento(TipoDocumento.DNI);

                    return toDTO(persona);
                })
                .orElseGet(() -> consultarFactiliza(dni));
    }


    // CONSULTAR API FACTILIZA


    private PersonaDTO consultarFactiliza(String dni) {

        if (reniecToken == null || reniecToken.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se ha configurado el token de Factiliza"
            );
        }

        String url = reniecUrl.endsWith("/")
                ? reniecUrl + dni
                : reniecUrl + "/" + dni;

        PersonaDTO.FactilizaResponse respuesta;

        try {

            respuesta = restClient.get()
                    .uri(url)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + reniecToken.trim()
                    )
                    .retrieve()
                    .body(PersonaDTO.FactilizaResponse.class);

        } catch (HttpClientErrorException.NotFound e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "DNI no encontrado en Factiliza"
            );

        } catch (HttpClientErrorException.Unauthorized e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Factiliza rechazó el token de acceso"
            );

        } catch (RestClientException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Error al consultar Factiliza",
                    e
            );
        }

        // Validar respuesta
        if (respuesta == null
                || !respuesta.success()
                || respuesta.data() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Factiliza no devolvió datos válidos"
            );
        }

        PersonaDTO.FactilizaData data = respuesta.data();

        if (!dni.equals(data.dni())
                || data.nombres() == null
                || data.nombres().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Los datos recibidos son incorrectos"
            );
        }

        // Crear persona desde la API
        Persona persona = new Persona();

        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento(dni);
        persona.setNombre(limpiar(data.nombres()));

        persona.setApellidoPaterno(
                textoOpcional(data.apellidoPaterno()));

        persona.setApellidoMaterno(
                textoOpcional(data.apellidoMaterno()));

        // Normalizar sexo
        persona.setSexo(
                normalizarSexo(data.sexo(), false));

        // Obtener dirección
        String direccion = textoOpcional(
                data.direccionCompleta());

        if (direccion == null) {
            direccion = textoOpcional(data.direccion());
        }

        persona.setDireccion(direccion);

        // Factiliza no proporciona teléfono
        persona.setTelefono(null);


        return toDTO(personaRepository.save(persona));
    }


    // BUSCAR PERSONA


    private Persona buscarPersona(Long id) {

        return personaRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Persona no encontrada"
                        )
                );
    }


    // VALIDACIONES


    private void validarDni(String dni) {

        if (dni == null || !dni.matches("\\d{8}")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El DNI debe tener exactamente 8 dígitos"
            );
        }
    }

    private void validarNombre(String nombre) {

        if (nombre == null
                || nombre.isBlank()
                || nombre.length() > 150) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio y debe tener máximo 150 caracteres"
            );
        }
    }

    private void validarTipoDocumento(TipoDocumento tipo) {

        if (tipo != null && tipo != TipoDocumento.DNI) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Este registro solo admite documentos DNI"
            );
        }
    }


    private String normalizarSexo(
            String sexo,
            boolean entradaManual) {

        if (sexo == null || sexo.isBlank()) {
            return null;
        }

        String valor = sexo.trim()
                .toUpperCase(Locale.ROOT);

        String resultado = switch (valor) {

            case "M", "MASCULINO" -> "MASCULINO";

            case "F", "FEMENINO" -> "FEMENINO";

            default -> null;
        };

        if (resultado == null && entradaManual) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El sexo debe ser MASCULINO o FEMENINO"
            );
        }

        return resultado;
    }


    // CONVERTIR ENTIDAD A DTO


    private PersonaDTO toDTO(Persona p) {

        return new PersonaDTO(
                p.getIdPersona(),
                p.getNumeroDocumento(),
                p.getNombre(),
                p.getTelefono(),
                p.getDireccion(),
                p.getApellidoPaterno(),
                p.getApellidoMaterno(),
                p.getTipoDocumento(),
                p.getSexo()
        );
    }


    // MÉTODOS AUXILIARES


    private String limpiar(String texto) {

        return texto != null ? texto.trim() : "";
    }

    private String textoOpcional(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}
