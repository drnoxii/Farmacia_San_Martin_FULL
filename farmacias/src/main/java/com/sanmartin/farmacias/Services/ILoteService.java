package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.LoteDTO;

import java.util.List;
import java.util.Optional;

public interface ILoteService {

    List<LoteDTO> listarTodo();

    Optional<LoteDTO> buscarPorId(Long id);

    List<LoteDTO> listarPorProducto(Long idProducto);

    List<LoteDTO> listarActivos();

    List<LoteDTO> listarPorVencer(int dias);
}
