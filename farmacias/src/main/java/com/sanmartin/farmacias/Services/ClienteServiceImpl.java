package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.ClienteDTO;
import com.sanmartin.farmacias.Entity.Cliente;
import com.sanmartin.farmacias.Entity.Persona;
import com.sanmartin.farmacias.Repository.ClienteRepository;
import com.sanmartin.farmacias.Repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements IClienteService{

    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository, PersonaRepository personaRepository) {
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    public List<ClienteDTO> listarTodo() {
        return clienteRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<ClienteDTO> buscarPorId(Long id) {
        return clienteRepository.findById(id).map(this::toDto);
    }

    @Override
    public Optional<ClienteDTO> buscarPorDocumento(String numeroDocumento) {
        return clienteRepository.findByPersonaNumeroDocumento(numeroDocumento)
                .map(this::toDto);
    }


    @Override
    @Transactional
    public ClienteDTO registrar(ClienteDTO dto) {

        // 1. Verificar que no exista un cliente con ese documento
        clienteRepository.findByPersonaNumeroDocumento(dto.numeroDocumento())
                .ifPresent(c -> {
                    throw new RuntimeException(
                            "Ya existe un cliente con ese documento"
                    );
                });

        // 2. Buscar la persona en Oracle
        // Si Factiliza ya la registró, reutilizarla
        Persona persona = personaRepository
                .findByNumeroDocumento(dto.numeroDocumento())
                .orElseGet(() -> {

                    Persona p = new Persona();

                    p.setNumeroDocumento(dto.numeroDocumento());
                    p.setNombre(dto.nombre());
                    p.setTelefono(dto.telefono());
                    p.setDireccion(dto.direccion());

                    return personaRepository.save(p);
                });

        // 3. Guardar el sexo seleccionado manualmente
        if (dto.sexo() != null && !dto.sexo().isBlank()) {
            persona.setSexo(dto.sexo());
            personaRepository.save(persona);
        }

        // 4. Registrar al cliente utilizando esa persona
        Cliente cliente = new Cliente();
        cliente.setPersona(persona);

        return toDto(clienteRepository.save(cliente));
    }


    private ClienteDTO toDto(Cliente c) {
        Persona p = c.getPersona();
        return new ClienteDTO(
                c.getIdCliente(),
                p.getNumeroDocumento(),
                p.getNombre(),
                p.getTelefono(),
                p.getDireccion(),
                p.getSexo()
        );
    }
}
