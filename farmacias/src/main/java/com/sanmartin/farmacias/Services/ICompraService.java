package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.CompraRequestDTO;
import com.sanmartin.farmacias.Dto.CompraResponseDTO;
import com.sanmartin.farmacias.Entity.EstadoCompra;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ICompraService {
    List<CompraResponseDTO> listarTodo();
    Optional<CompraResponseDTO> buscarPorId(Long id);
    CompraResponseDTO registrar(CompraRequestDTO dto);
    CompraResponseDTO actualizarEstado(Long id, EstadoCompra estadoCompra);

    List<CompraResponseDTO> listarPorIdProveedor(Long idProveedor);
    List<CompraResponseDTO> listarPorNombreProveedor(String nombreProveedor);
    List<CompraResponseDTO> listarPornombreProducto(String nombreProducto);
}
