package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.CompraRequestDTO;
import com.sanmartin.farmacias.Dto.CompraResponseDTO;
import com.sanmartin.farmacias.Dto.DetalleCompraRequestDTO;
import com.sanmartin.farmacias.Dto.DetalleCompraResponseDTO;
import com.sanmartin.farmacias.Entity.*;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EntityManager em;

    public CompraServiceImpl(CompraRepository compraRepository, DetalleCompraRepository detalleCompraRepository, LoteRepository loteRepository, ProveedorRepository proveedorRepository, UsuarioRepository usuarioRepository, ProductoRepository productoRepository, EntityManager em) {
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.loteRepository = loteRepository;
        this.proveedorRepository = proveedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.em = em;
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
    @Transactional
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

        // 4. Crear Compra (siempre en PENDIENTE)
        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);
        compra.setFechaCompra(LocalDateTime.now());
        compra.setTotalCompra(total);
        compra.setEstado(EstadoCompra.PENDIENTE);
        compra = compraRepository.save(compra);

        // 5. Por cada detalle: crear DetalleCompra + Lote (con stock 0 mientras esté PENDIENTE)
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

            // 5.2 Lote (1:1 con el detalle, stock en 0 hasta que sea RECIBIDA)
            Lote lote = new Lote();
            lote.setProducto(producto);
            lote.setDetalleCompra(detalle);
            lote.setNumeroLote(d.numeroLote());
            lote.setStockLote(0);
            lote.setFechaVencimiento(d.fechaVencimiento());
            lote.setEstado(EstadoLote.ACTIVO);
            loteRepository.save(lote);
        }

        return toDto(compra);
    }

    @Override
    @Transactional
    public CompraResponseDTO actualizarEstado(Long id, EstadoCompra nuevoEstado) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));

        EstadoCompra estadoActual = compra.getEstado();

        if (estadoActual == nuevoEstado) {
            return toDto(compra);
        }

        if (estadoActual == EstadoCompra.ANULADA) {
            throw new IllegalStateException("No se puede cambiar el estado de una compra que ya está ANULADA.");
        }

        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraIdCompra(compra.getIdCompra());

        // Caso 1: PENDIENTE -> RECIBIDA (Ingresa mercadería, se suma stock)
        if (estadoActual == EstadoCompra.PENDIENTE && nuevoEstado == EstadoCompra.RECIBIDA) {
            for (DetalleCompra detalle : detalles) {
                Lote lote = loteRepository.findByDetalleCompraIdDetalleCompra(detalle.getIdDetalleCompra())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Lote no encontrado para el detalle de compra ID: " + detalle.getIdDetalleCompra()));

                int stockAnterior = lote.getStockLote() != null ? lote.getStockLote() : 0;
                int nuevoStock = stockAnterior + detalle.getCantidad();
                lote.setStockLote(nuevoStock);
                lote.setEstado(EstadoLote.ACTIVO);
                loteRepository.save(lote);

                MovimientoInventario mov = new MovimientoInventario();
                mov.setLote(lote);
                mov.setTipoMovimiento(TipoMovimientoInventario.ENTRADA);
                mov.setCantidad(BigDecimal.valueOf(detalle.getCantidad()));
                mov.setStockAnterior(BigDecimal.valueOf(stockAnterior));
                mov.setStockNuevo(BigDecimal.valueOf(nuevoStock));
                mov.setReferenciaTipo(ReferenciaTipo.COMPRA);
                mov.setReferenciaId(compra.getIdCompra());
                mov.setUsuario(compra.getUsuario());
                em.persist(mov);
            }
        }

        // Caso 2: RECIBIDA -> ANULADA (Se anula compra que ya sumó, se debe restar stock)
        else if (estadoActual == EstadoCompra.RECIBIDA && nuevoEstado == EstadoCompra.ANULADA) {
            // Validación previa: verificar que haya stock suficiente en cada lote para revertir
            for (DetalleCompra detalle : detalles) {
                Lote lote = loteRepository.findByDetalleCompraIdDetalleCompra(detalle.getIdDetalleCompra())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Lote no encontrado para el detalle de compra ID: " + detalle.getIdDetalleCompra()));

                int stockActual = lote.getStockLote() != null ? lote.getStockLote() : 0;
                if (stockActual < detalle.getCantidad()) {
                    throw new IllegalStateException(String.format(
                            "No se puede anular la compra: el lote '%s' del producto '%s' ya tiene ventas registradas (Stock disponible: %d, a restar: %d).",
                            lote.getNumeroLote(),
                            detalle.getProducto().getNombreProducto(),
                            stockActual,
                            detalle.getCantidad()
                    ));
                }
            }

            // Si todos los lotes tienen stock suficiente, se descuenta
            for (DetalleCompra detalle : detalles) {
                Lote lote = loteRepository.findByDetalleCompraIdDetalleCompra(detalle.getIdDetalleCompra()).get();
                int stockAnterior = lote.getStockLote() != null ? lote.getStockLote() : 0;
                int nuevoStock = stockAnterior - detalle.getCantidad();
                lote.setStockLote(nuevoStock);

                if (nuevoStock == 0) {
                    lote.setEstado(EstadoLote.AGOTADO);
                }
                loteRepository.save(lote);

                MovimientoInventario mov = new MovimientoInventario();
                mov.setLote(lote);
                mov.setTipoMovimiento(TipoMovimientoInventario.SALIDA);
                mov.setCantidad(BigDecimal.valueOf(detalle.getCantidad()));
                mov.setStockAnterior(BigDecimal.valueOf(stockAnterior));
                mov.setStockNuevo(BigDecimal.valueOf(nuevoStock));
                mov.setReferenciaTipo(ReferenciaTipo.COMPRA);
                mov.setReferenciaId(compra.getIdCompra());
                mov.setUsuario(compra.getUsuario());
                em.persist(mov);
            }
        }
        // Caso 3: PENDIENTE -> ANULADA (Se cancela antes de recibir, nunca sumó, no se resta nada)
        else if (estadoActual == EstadoCompra.PENDIENTE && nuevoEstado == EstadoCompra.ANULADA) {
            for (DetalleCompra detalle : detalles) {
                loteRepository.findByDetalleCompraIdDetalleCompra(detalle.getIdDetalleCompra()).ifPresent(lote -> {
                    if (lote.getStockLote() == null || lote.getStockLote() == 0) {
                        lote.setEstado(EstadoLote.AGOTADO);
                        loteRepository.save(lote);
                    }
                });
            }
        }
        // Caso 4: RECIBIDA -> PENDIENTE (No permitido)
        else if (estadoActual == EstadoCompra.RECIBIDA && nuevoEstado == EstadoCompra.PENDIENTE) {
            throw new IllegalStateException("Una compra RECIBIDA no puede regresar a PENDIENTE. Si desea cancelarla, cámbiela a ANULADA.");
        }

        compra.setEstado(nuevoEstado);
        return toDto(compraRepository.save(compra));
    }

    @Override
    public List<CompraResponseDTO> listarPorIdProveedor(Long idProveedor) {
        return compraRepository.findByProveedor_idProveedor(idProveedor).stream().map(this::toDto).toList();
    }

    @Override
    public List<CompraResponseDTO> listarPorNombreProveedor(String nombreProveedor) {
        return compraRepository.findByProveedor_razonSocial(nombreProveedor).stream().map(this::toDto).toList();
    }

    @Override
    public List<CompraResponseDTO> listarPornombreProducto(String nombreProducto) {
        return compraRepository.findDistinctBydetalles_producto_nombreProductoContainingIgnoreCase(nombreProducto).stream().map(this::toDto).toList();
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
