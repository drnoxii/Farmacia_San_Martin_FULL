package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.ProductoDto;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface IProductoServices {
    List<ProductoDto> listarProductos();
    Optional<ProductoDto> buscarProductoPorId(Long id);
    ProductoDto registrarProducto(ProductoDto producto);
    Optional<ProductoDto> actualizarProducto(Long id, ProductoDto producto);
    boolean eliminarProducto(Long id);

    Optional<ProductoDto> buscarPorNombre(String nombre);
    List<ProductoDto> buscarPorCategoria(Long idCategoria);
    List<ProductoDto> buscarPorLaboratorio(Long idLaboratorio);
    List<ProductoDto> buscarPorEstadoGeneral(EstadoGeneral estadoGeneral);
}
