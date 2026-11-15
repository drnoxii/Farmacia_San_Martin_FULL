package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.InventarioDto;
import com.sanmartin.farmacias.Dto.ProductoDto;
import com.sanmartin.farmacias.Entity.*;
import com.sanmartin.farmacias.Exception.DuplicateResourceException;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.CategoriaRepository;
import com.sanmartin.farmacias.Repository.LaboratorioRepository;
import com.sanmartin.farmacias.Repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
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
    @Transactional(readOnly = true)
    public Optional<ProductoDto> buscarProductoPorId(Long id) {
        return productoRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional
    public ProductoDto registrarProducto(ProductoDto producto) {
        boolean duplicado = productoRepository.existeProductoDuplicado(
                producto.nombreProducto(),
                producto.idLaboratorio(),
                producto.presentacion(),
                -1L
        );

        if (duplicado) {
            throw new DuplicateResourceException(
                    "Ya existe un producto con el mismo nombre, laboratorio y presentación"
            );
        }

        Categoria categoria= categoriaRepository.findById(producto.idCategoria()).
                orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        Laboratorio laboratorio= laboratorioRepository.findById(producto.idLaboratorio()).
                orElseThrow(() -> new ResourceNotFoundException("Laboratorio no encontrado"));

        Producto prod= new Producto();

        prod.setNombreProducto(producto.nombreProducto());
        prod.setDescripcion(producto.descripcion());
        prod.setPresentacion(producto.presentacion());
        prod.setCategoria(categoria);
        prod.setLaboratorio(laboratorio);
        prod.setPrecioVenta(producto.precioVenta());
        prod.setStockMinimo(producto.stockMinimo());
        prod.setEstadoGeneral(producto.estadoGeneral());
        // Primero guardamos el producto para obtener su ID
        prod = productoRepository.saveAndFlush(prod);

        // Generamos el código usando el ID asignado por Oracle
        String codigoGenerado =
                String.format("PROD-%06d", prod.getIdProducto());

        prod.setCodigo(codigoGenerado);

        // Persistimos el código generado
        productoRepository.flush();

        return convertToDto(prod);
    }

    @Override
    @Transactional
    public Optional<ProductoDto> actualizarProducto(Long id, ProductoDto producto) {
        return productoRepository.findById(id).map(prod -> {

            boolean duplicado = productoRepository.existeProductoDuplicado(
                    producto.nombreProducto(),
                    producto.idLaboratorio(),
                    producto.presentacion(),
                    id
            );

            if (duplicado) {
                throw new DuplicateResourceException(
                        "Ya existe otro producto con el mismo nombre, laboratorio y presentación"
                );
            }
            Categoria categoria= categoriaRepository.findById(producto.idCategoria())
                    .orElseThrow(() -> new DuplicateResourceException("Categoria no encontrada"));
            Laboratorio laboratorio= laboratorioRepository.findById(producto.idLaboratorio())
                    .orElseThrow(() -> new DuplicateResourceException("Laboratorio no encontrado"));

            prod.setNombreProducto(producto.nombreProducto());
            prod.setDescripcion(producto.descripcion());
            prod.setCategoria(categoria);
            prod.setPresentacion(producto.presentacion());
            prod.setLaboratorio(laboratorio);

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
    @Transactional(readOnly = true)
    public List<ProductoDto> buscarPorNombre(String nombre) {
        return productoRepository
                .findByNombreProductoIgnoreCase(nombre.trim())
                .stream()
                .map(this::convertToDto)
                .toList();
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
    @Transactional(readOnly = true)
    public List<ProductoDto> buscarPorEstadoGeneral(EstadoGeneral estadoGeneral) {
        return productoRepository.findByEstadoGeneral(estadoGeneral).stream().map(this::convertToDto).toList();
    }

    @Override
    public List<InventarioDto> consultarInventario() {
        return productoRepository.consultarInventario(
                EstadoGeneral.ACTIVO,
                EstadoLote.ACTIVO,
                LocalDate.now(ZoneId.of("America/Lima"))
        );
    }

    


    private ProductoDto convertToDto(Producto producto){
        return new ProductoDto(
                producto.getIdProducto(),
                producto.getCodigo(),
                producto.getNombreProducto(),
                producto.getDescripcion(),
                producto.getPresentacion(),
                producto.getCategoria().getIdCategoria(),
                producto.getLaboratorio().getIdLaboratorio(),
                producto.getPrecioVenta(),
                producto.getStockMinimo(),
                producto.getEstadoGeneral(),

                producto.getCategoria().getNombreCategoria(),
                producto.getLaboratorio().getNombreLaboratorio()
        );
    }

}
