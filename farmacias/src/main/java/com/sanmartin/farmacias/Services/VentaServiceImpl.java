package com.sanmartin.farmacias.Services;


import com.sanmartin.farmacias.Dto.DetalleVentaRequestDTO;
import com.sanmartin.farmacias.Dto.DetalleVentaResponseDTO;
import com.sanmartin.farmacias.Dto.VentaRequestDTO;
import com.sanmartin.farmacias.Dto.VentaResponseDTO;
import com.sanmartin.farmacias.Entity.*;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VentaServiceImpl implements IVentaService{

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final LoteRepository loteRepository;
    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    public VentaServiceImpl(VentaRepository ventaRepository, DetalleVentaRepository detalleVentaRepository, LoteRepository loteRepository, CajaRepository cajaRepository, UsuarioRepository usuarioRepository, ClienteRepository clienteRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.loteRepository = loteRepository;
        this.cajaRepository = cajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
    }


    @Override
    public List<VentaResponseDTO> listarTodo() {
        return ventaRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<VentaResponseDTO> buscarPorId(Long id) {
        return ventaRepository.findById(id).map(this::toDto);
    }

    @Override
    public VentaResponseDTO registrar(VentaRequestDTO dto) {
        Caja caja = cajaRepository.findById(dto.idCaja())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Caja no encontrada con ID: " + dto.idCaja()));

        if (caja.getEstado() != EstadoCaja.ABIERTA) {
            throw new RuntimeException("La caja no está abierta");
        }

        // 2. Validar usuario
        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + dto.idUsuario()));

        // 3. Validar cliente (opcional)
        Cliente cliente = null;
        if (dto.idCliente() != null) {
            cliente = clienteRepository.findById(dto.idCliente())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado con ID: " + dto.idCliente()));
        }

        // 4. Calcular total y validar stock
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVentaRequestDTO d : dto.detalles()) {

            Lote lote = loteRepository.findById(d.idLote())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Lote no encontrado con ID: " + d.idLote()));

            if (lote.getEstado() != EstadoLote.ACTIVO) {
                throw new RuntimeException(
                        "El lote " + lote.getNumeroLote() + " no está activo");
            }


            BigDecimal stockDisponible = BigDecimal.valueOf(lote.getStockLote());
            if (stockDisponible.compareTo(d.cantidad()) < 0) {
                throw new RuntimeException(
                        "Stock insuficiente en el lote " + lote.getNumeroLote() +
                                ". Disponible: " + lote.getStockLote() +
                                ", solicitado: " + d.cantidad());
            }

            BigDecimal precioUnitario = lote.getProducto().getPrecioVenta();
            BigDecimal subtotal = precioUnitario.multiply(d.cantidad());
            total = total.add(subtotal);
        }

        // 5. Crear Venta
        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setCaja(caja);
        venta.setUsuario(usuario);
        venta.setNumeroVenta(generarNumeroVenta());
        venta.setTipoComprobante(dto.tipoComprobante());
        venta.setMetodoPago(dto.metodoPago());
        venta.setTotal(total);
        venta.setEstado(EstadoVenta.EMITIDA);
        venta.setFechaVenta(LocalDateTime.now());
        venta = ventaRepository.save(venta);

        // 6. Crear detalles y descontar stock
        for (DetalleVentaRequestDTO d : dto.detalles()) {

            Lote lote = loteRepository.findById(d.idLote()).get();

            BigDecimal precioUnitario = lote.getProducto().getPrecioVenta();
            BigDecimal subtotal = precioUnitario.multiply(d.cantidad());

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setLote(lote);
            detalle.setCantidad(d.cantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            detalle.setEstado(EstadoDetalleVenta.V);
            detalleVentaRepository.save(detalle);

            // Descontar stock del lote (Integer - BigDecimal)
            BigDecimal nuevoStock = BigDecimal.valueOf(lote.getStockLote())
                    .subtract(d.cantidad());
            lote.setStockLote(nuevoStock.intValue());

            if (lote.getStockLote() == 0) {
                lote.setEstado(EstadoLote.AGOTADO);
            }
            loteRepository.save(lote);
        }

        return toDto(venta);
    }

    private String generarNumeroVenta() {
        long siguiente = ventaRepository.count() + 1;
        return String.format("V%08d", siguiente);
    }

    private VentaResponseDTO toDto(Venta v) {

        List<DetalleVentaResponseDTO> detalles = detalleVentaRepository
                .findByVenta_idVenta(v.getIdVenta())
                .stream()
                .map(this::toDetalleDto)
                .toList();

        return new VentaResponseDTO(
                v.getIdVenta(),
                v.getNumeroVenta(),
                v.getCliente() != null ? v.getCliente().getIdCliente() : null,
                v.getCliente() != null ? v.getCliente().getPersona().getNombre() : null,
                v.getCaja().getIdCaja(),
                v.getUsuario().getIdUsuario(),
                v.getUsuario().getPersona().getNombre(),
                v.getTipoComprobante(),
                v.getMetodoPago(),
                v.getTotal(),
                v.getEstado(),
                v.getFechaVenta(),
                detalles
        );
    }

    private DetalleVentaResponseDTO toDetalleDto(DetalleVenta d) {

        Producto producto = d.getLote().getProducto();

        return new DetalleVentaResponseDTO(
                d.getIdDetalleVenta(),
                d.getLote().getIdLote(),
                d.getLote().getNumeroLote(),
                d.getLote().getFechaVencimiento(),
                producto.getIdProducto(),
                producto.getNombreProducto(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getSubtotal(),
                d.getEstado()
        );
    }
}
