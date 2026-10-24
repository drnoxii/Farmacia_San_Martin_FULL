package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.CajaAperturaDTO;
import com.sanmartin.farmacias.Dto.CajaCierreDTO;
import com.sanmartin.farmacias.Dto.CajaDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ICajaService {

    List<CajaDTO> listarTodo();

    Optional<CajaDTO> buscarPorId(Long id);

    List<CajaDTO> listarAbiertas();

    List<CajaDTO> listarPorUsuario(Long idUsuario);

    CajaDTO abrir(CajaAperturaDTO dto);

    CajaDTO cerrar(Long idCaja, CajaCierreDTO dto);
}
