package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.ClienteDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface IClienteService {

    List<ClienteDTO> listarTodo();

    Optional<ClienteDTO> buscarPorId(Long id);

    Optional<ClienteDTO> buscarPorDocumento(String numeroDocumento);

    ClienteDTO registrar(ClienteDTO dto);
}
