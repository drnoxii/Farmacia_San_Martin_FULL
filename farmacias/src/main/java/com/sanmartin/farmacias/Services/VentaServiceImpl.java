package com.sanmartin.farmacias.Services;


import com.sanmartin.farmacias.Dto.DetalleVentaRequestDTO;
import com.sanmartin.farmacias.Dto.DetalleVentaResponseDTO;
import com.sanmartin.farmacias.Dto.VentaRequestDTO;
import com.sanmartin.farmacias.Dto.VentaResponseDTO;
import com.sanmartin.farmacias.Entity.*;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VentaServiceImpl implements IVentaService{

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final LoteRepository loteRepository;
    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final MovimientoInventarioProcedure movimientoProcedure;

    public VentaServiceImpl(VentaRepository ventaRepository, DetalleVentaRepository detalleVentaRepository, LoteRepository loteRepository, CajaRepository cajaRepository, UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, MovimientoInventarioProcedure movimientoProcedure) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.loteRepository = loteRepository;
        this.cajaRepository = cajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.movimientoProcedure = movimientoProcedure;
    }

    @Override
    public List<VentaResponseDTO> listarTodo() {
        return ventaRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VentaResponseDTO> buscarPorId(Long id) {
        return ventaRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional
    public VentaResponseDTO registrar(
            VentaRequestDTO dto,
            Long idUsuario) {
        Caja caja = cajaRepository.findById(dto.idCaja())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Caja no encontrada con ID: " + dto.idCaja()));

        if (caja.getEstado() != EstadoCaja.ABIERTA) {
            throw new RuntimeException("La caja no está abierta");
        }

        // 2. Validar usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + idUsuario));

        if (!caja.getUsuario().getIdUsuario()
                .equals(usuario.getIdUsuario())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario de la venta no corresponde al propietario de la caja"
            );
        }

        // 3. Validar cliente (opcional)
        Cliente cliente = null;
        if (dto.idCliente() != null) {
            cliente = clienteRepository.findById(dto.idCliente())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado con ID: " + dto.idCliente()));
        }

        // 4. Calcular total y validar stock

        BigDecimal total = BigDecimal.ZERO;

        Set<Long> lotesEnVenta = new HashSet<>();

// Ordenar los lotes por ID para adquirir
// los bloqueos siempre en el mismo orden.
        List<DetalleVentaRequestDTO> detallesOrdenados =
                dto.detalles().stream()
                        .sorted(Comparator.comparing(
                                DetalleVentaRequestDTO::idLote
                        ))
                        .toList();

        for (DetalleVentaRequestDTO d : detallesOrdenados) {


            // NUEVO: impedir lotes repetidos
            if (!lotesEnVenta.add(d.idLote())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El lote con ID " + d.idLote() +
                                " está repetido en la venta. " +
                                "Debes agruparlo en un solo detalle."
                );
            }


            // Validar que la cantidad sea un entero positivo
            try {
                if (d.cantidad() == null ||
                        d.cantidad().intValueExact() <= 0) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "La cantidad debe ser mayor a cero"
                    );
                }
            } catch (ArithmeticException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La cantidad debe ser un número entero positivo"
                );
            }



            Lote lote = loteRepository
                    .buscarPorIdConBloqueo(d.idLote())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Lote no encontrado con ID: " + d.idLote()
                    ));


            // 1. Validar estado del lote
            if (lote.getEstado() != EstadoLote.ACTIVO) {
                throw new RuntimeException(
                        "El lote " + lote.getNumeroLote() + " no está activo");
            }

            // 2. NUEVO: validar fecha de vencimiento
            if (lote.getFechaVencimiento().isBefore(LocalDate.now())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No se puede vender el lote " +
                                lote.getNumeroLote() +
                                " porque venció el " +
                                lote.getFechaVencimiento()
                );
            }

            // 3. Validar stock disponible
            BigDecimal stockDisponible =
                    BigDecimal.valueOf(lote.getStockLote());

            if (stockDisponible.compareTo(d.cantidad()) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente en el lote " +
                                lote.getNumeroLote() +
                                ". Disponible: " + lote.getStockLote() +
                                ", solicitado: " + d.cantidad()
                );
            }

            BigDecimal precioUnitario =
                    lote.getProducto().getPrecioVenta();

            BigDecimal subtotal =
                    precioUnitario.multiply(d.cantidad());

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

            int stockAnterior = lote.getStockLote();

            // Cantidad ya validada como entero positivo
            int cantidadEntera = d.cantidad().intValueExact();

            // Calcular nuevo stock
            int nuevoStock = stockAnterior - cantidadEntera;

            lote.setStockLote(nuevoStock);

            if (nuevoStock == 0) {
                lote.setEstado(EstadoLote.AGOTADO);
            }

            loteRepository.save(lote);


            // =====================================
            // 2. REGISTRAR MOVIMIENTO CON ORACLE
            // =====================================

            movimientoProcedure.registrar(
                    lote.getIdLote(),
                    TipoMovimientoInventario.SALIDA,
                    BigDecimal.valueOf(cantidadEntera),
                    BigDecimal.valueOf(stockAnterior),
                    BigDecimal.valueOf(nuevoStock),
                    ReferenciaTipo.VENTA,
                    venta.getIdVenta(),
                    usuario.getIdUsuario()
            );
        }



        return toDto(venta);
    }


    private String generarNumeroVenta() {
        Long siguiente = ventaRepository.siguienteNumeroVenta();
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
                producto.getCodigo(),
                producto.getNombreProducto(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getSubtotal(),
                d.getEstado()
        );
    }
}
