package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.CompraRequestDTO;
import com.sanmartin.farmacias.Dto.CompraResponseDTO;
import com.sanmartin.farmacias.Dto.DetalleCompraRequestDTO;
import com.sanmartin.farmacias.Dto.DetalleCompraResponseDTO;
import com.sanmartin.farmacias.Entity.*;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CompraServiceImpl implements ICompraService{

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final LoteRepository loteRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public CompraServiceImpl(CompraRepository compraRepository, DetalleCompraRepository detalleCompraRepository, LoteRepository loteRepository, ProveedorRepository proveedorRepository, UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.loteRepository = loteRepository;
        this.proveedorRepository = proveedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public List<CompraResponseDTO> listarTodo() {
        return compraRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<CompraResponseDTO> buscarPorId(Long id) {
        return compraRepository.findById(id).map(this::toDto);
    }

    @Override
    public CompraResponseDTO registrar(CompraRequestDTO dto) {
        // 1. Validar proveedor
        Proveedor proveedor = proveedorRepository.findById(dto.idProveedor())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proveedor no encontrado con ID: " + dto.idProveedor()));

        // 2. Validar usuario
        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + dto.idUsuario()));

        // 3. Calcular total
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCompraRequestDTO d : dto.detalles()) {
            BigDecimal subtotal = d.precioCosto()
                    .multiply(BigDecimal.valueOf(d.cantidad()));
            total = total.add(subtotal);
        }

        // 4. Crear Compra
        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);
        compra.setFechaCompra(LocalDateTime.now());
        compra.setTotalCompra(total);
        compra.setEstado(EstadoCompra.RECIBIDA);
        compra = compraRepository.save(compra);

        // 5. Por cada detalle: crear DetalleCompra + Lote
        for (DetalleCompraRequestDTO d : dto.detalles()) {

            Producto producto = productoRepository.findById(d.idProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + d.idProducto()));

            BigDecimal subtotal = d.precioCosto()
                    .multiply(BigDecimal.valueOf(d.cantidad()));

            // 5.1 DetalleCompra
            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setCantidad(d.cantidad());
            detalle.setPrecioCosto(d.precioCosto());
            detalle.setSubtotal(subtotal);
            detalle = detalleCompraRepository.save(detalle);

            // 5.2 Lote (1:1 con el detalle)
            Lote lote = new Lote();
            lote.setProducto(producto);
            lote.setDetalleCompra(detalle);
            lote.setNumeroLote(d.numeroLote());
            lote.setStockLote(d.cantidad());
            lote.setFechaVencimiento(d.fechaVencimiento());
            lote.setEstado(EstadoLote.ACTIVO);
            loteRepository.save(lote);
        }

        return toDto(compra);
    }

    private CompraResponseDTO toDto(Compra c) {

        List<DetalleCompraResponseDTO> detalles = detalleCompraRepository
                .findByCompraIdCompra(c.getIdCompra())
                .stream()
                .map(this::toDetalleDto)
                .toList();

        return new CompraResponseDTO(
                c.getIdCompra(),
                c.getProveedor().getIdProveedor(),
                c.getProveedor().getRazonSocial(),
                c.getUsuario().getIdUsuario(),
                c.getUsuario().getPersona().getNombre(),
                c.getFechaCompra(),
                c.getTotalCompra(),
                c.getEstado(),
                detalles
        );
    }

    private DetalleCompraResponseDTO toDetalleDto(DetalleCompra d) {

        Lote lote = loteRepository
                .findByDetalleCompraIdDetalleCompra(d.getIdDetalleCompra())
                .orElse(null);

        return new DetalleCompraResponseDTO(
                d.getIdDetalleCompra(),
                d.getProducto().getIdProducto(),
                d.getProducto().getNombreProducto(),
                d.getCantidad(),
                d.getPrecioCosto(),
                d.getSubtotal(),
                lote != null ? lote.getNumeroLote() : null,
                lote != null ? lote.getFechaVencimiento() : null,
                lote != null ? lote.getEstado() : null
        );
    }
}
