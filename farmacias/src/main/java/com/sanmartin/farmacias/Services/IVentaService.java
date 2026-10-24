package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.VentaRequestDTO;
import com.sanmartin.farmacias.Dto.VentaResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface IVentaService {

    List<VentaResponseDTO> listarTodo();

    Optional<VentaResponseDTO> buscarPorId(Long id);

    VentaResponseDTO registrar(VentaRequestDTO dto);
}
