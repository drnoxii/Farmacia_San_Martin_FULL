package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.DevolucionVentaRequestDTO;
import com.sanmartin.farmacias.Dto.DevolucionVentaResponseDTO;
import com.sanmartin.farmacias.Entity.*;
import com.sanmartin.farmacias.Repository.DetalleVentaRepository;
import com.sanmartin.farmacias.Repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;

@Service
public class DevolucionVentaService {
    private final EntityManager em;
    private final DetalleVentaRepository detallesRepository;
    private final UsuarioRepository usuarios;

    public DevolucionVentaService(EntityManager em, DetalleVentaRepository detallesRepository,
                                 UsuarioRepository usuarios) {
        this.em = em;
        this.detallesRepository = detallesRepository;
        this.usuarios = usuarios;
    }

    @Transactional
    public DevolucionVentaResponseDTO devolver(Long idVenta, DevolucionVentaRequestDTO dto) {
        Venta venta = em.find(Venta.class, idVenta, LockModeType.PESSIMISTIC_WRITE);
        if (venta == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada.");
        if (venta.getEstado() != EstadoVenta.EMITIDA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La venta ya está anulada.");
        }
        Usuario usuario = usuarios.findById(dto.idUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado."));
        var detalles = detallesRepository.findByVenta_idVenta(idVenta);
        var ids = new HashSet<>(dto.idDetalles());
        var seleccionados = detalles.stream().filter(d -> ids.contains(d.getIdDetalleVenta())).toList();
        if (ids.isEmpty() || ids.size() != dto.idDetalles().size() || seleccionados.size() != ids.size()
                || seleccionados.stream().anyMatch(d -> d.getEstado() != EstadoDetalleVenta.V)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Selecciona detalles de esta venta que aún no hayan sido devueltos.");
        }
        for (DetalleVenta detalle : seleccionados) {
            try {
                if (detalle.getCantidad().intValueExact() <= 0) throw new ArithmeticException();
            } catch (ArithmeticException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La devolución requiere una cantidad entera positiva por detalle.");
            }
        }

        // Orden estable para bloquear lotes compartidos entre devoluciones concurrentes.
        var lotes = new LinkedHashMap<Long, Lote>();
        seleccionados.stream().map(d -> d.getLote().getIdLote()).distinct().sorted().forEach(id -> {
            Lote lote = em.find(Lote.class, id, LockModeType.PESSIMISTIC_WRITE);
            em.refresh(lote, LockModeType.PESSIMISTIC_WRITE);
            lotes.put(id, lote);
        });

        BigDecimal importe = BigDecimal.ZERO;
        for (DetalleVenta detalle : seleccionados) {
            Lote lote = lotes.get(detalle.getLote().getIdLote());
            int anterior = lote.getStockLote();
            int nuevo = Math.addExact(anterior, detalle.getCantidad().intValueExact());
            lote.setStockLote(nuevo);
            if (lote.getFechaVencimiento().isBefore(LocalDate.now())) lote.setEstado(EstadoLote.VENCIDO);
            else if (lote.getEstado() == EstadoLote.AGOTADO) lote.setEstado(EstadoLote.ACTIVO);
            detalle.setEstado(EstadoDetalleVenta.D);
            importe = importe.add(detalle.getPrecioUnitario().multiply(detalle.getCantidad()));

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setLote(lote);
            movimiento.setTipoMovimiento(TipoMovimientoInventario.ENTRADA);
            movimiento.setCantidad(detalle.getCantidad());
            movimiento.setStockAnterior(BigDecimal.valueOf(anterior));
            movimiento.setStockNuevo(BigDecimal.valueOf(nuevo));
            movimiento.setReferenciaTipo(ReferenciaTipo.VENTA);
            movimiento.setReferenciaId(idVenta);
            movimiento.setUsuario(usuario);
            em.persist(movimiento);
        }
        if (detalles.stream().allMatch(d -> d.getEstado() == EstadoDetalleVenta.D)) {
            venta.setEstado(EstadoVenta.ANULADA);
        }
        // El importe original se conserva; los reportes descuentan los detalles devueltos.
        em.flush();
        var stock = lotes.values().stream().map(l -> new DevolucionVentaResponseDTO.StockRepuesto(
                l.getIdLote(), l.getProducto().getNombreProducto(), l.getNumeroLote(), l.getStockLote())).toList();
        return new DevolucionVentaResponseDTO(idVenta, importe, stock);
    }
}
