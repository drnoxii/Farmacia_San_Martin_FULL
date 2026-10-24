package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.ProductoDto;
import com.sanmartin.farmacias.Entity.Categoria;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Laboratorio;
import com.sanmartin.farmacias.Entity.Producto;
import com.sanmartin.farmacias.Exception.DuplicateResourceException;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.CategoriaRepository;
import com.sanmartin.farmacias.Repository.LaboratorioRepository;
import com.sanmartin.farmacias.Repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements IProductoServices{
    private final ProductoRepository productoRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, LaboratorioRepository laboratorioRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.laboratorioRepository = laboratorioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> listarProductos() {
        return productoRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<ProductoDto> buscarProductoPorId(Long id) {
        return productoRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional
    public ProductoDto registrarProducto(ProductoDto producto) {
        if (productoRepository.existsByNombreProducto(producto.nombreProducto())){
            throw new DuplicateResourceException("Ya existe un producto con el mismo nombre");
        }

        Categoria categoria= categoriaRepository.findById(producto.idCategoria()).
                orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        Laboratorio laboratorio= laboratorioRepository.findById(producto.idLaboratorio()).
                orElseThrow(() -> new ResourceNotFoundException("Laboratorio no encontrado"));

        Producto prod= new Producto();
        prod.setNombreProducto(producto.nombreProducto());
        prod.setDescripcion(producto.descripcion());
        prod.setCategoria(categoria);
        prod.setLaboratorio(laboratorio);
        prod.setPrecioCompra(producto.precioCompra());
        prod.setPrecioVenta(producto.precioVenta());
        prod.setStockMinimo(producto.stockMinimo());
        prod.setEstadoGeneral(producto.estadoGeneral());
        prod= productoRepository.save(prod);

        return convertToDto(prod);
    }

    @Override
    @Transactional
    public Optional<ProductoDto> actualizarProducto(Long id, ProductoDto producto) {
        return productoRepository.findById(id).map(prod -> {
            Categoria categoria= categoriaRepository.findById(producto.idCategoria())
                    .orElseThrow(() -> new DuplicateResourceException("Categoria no encontrada"));
            Laboratorio laboratorio= laboratorioRepository.findById(producto.idLaboratorio())
                    .orElseThrow(() -> new DuplicateResourceException("Laboratorio no encontrado"));

            prod.setNombreProducto(producto.nombreProducto());
            prod.setDescripcion(producto.descripcion());
            prod.setCategoria(categoria);
            prod.setLaboratorio(laboratorio);
            prod.setPrecioCompra(producto.precioCompra());
            prod.setPrecioVenta(producto.precioVenta());
            prod.setStockMinimo(producto.stockMinimo());
            prod.setEstadoGeneral(producto.estadoGeneral());
            return convertToDto(prod);
        });
    }

    @Override
    @Transactional
    public boolean eliminarProducto(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<ProductoDto> buscarPorNombre(String nombre) {
        return productoRepository.findBynombreProducto(nombre).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> buscarPorCategoria(Long idCategoria) {
        return productoRepository.findByCategoria_idCategoria(idCategoria).stream().map(this::convertToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> buscarPorLaboratorio(Long idLaboratorio) {
        return productoRepository.findByLaboratorio_idLaboratorio(idLaboratorio).stream().map(this::convertToDto).toList();
    }

    @Override
    public List<ProductoDto> buscarPorEstadoGeneral(EstadoGeneral estadoGeneral) {
        return productoRepository.findByEstadoGeneral(estadoGeneral).stream().map(this::convertToDto).toList();
    }

    private ProductoDto convertToDto(Producto producto){
        return new ProductoDto(
                producto.getIdProducto(),
                producto.getNombreProducto(),
                producto.getDescripcion(),
                producto.getCategoria().getIdCategoria(),
                producto.getLaboratorio().getIdLaboratorio(),
                producto.getPrecioCompra(),
                producto.getPrecioVenta(),
                producto.getStockMinimo(),
                producto.getEstadoGeneral()
        );
    }

}
