package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.DevolucionVentaRequestDTO;
import com.sanmartin.farmacias.Entity.*;
import com.sanmartin.farmacias.Repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DevolucionVentaServiceTest {
    EntityManager em = mock(EntityManager.class);
    DetalleVentaRepository detalles = mock(DetalleVentaRepository.class);
    UsuarioRepository usuarios = mock(UsuarioRepository.class);
    DevolucionVentaService service = new DevolucionVentaService(em, detalles, usuarios);
    Venta venta = new Venta();
    Lote lote = new Lote();
    DetalleVenta detalle = new DetalleVenta();

    @BeforeEach void configurar() {
        venta.setIdVenta(1L);
        venta.setTotal(new BigDecimal("20"));
        lote.setIdLote(2L);
        lote.setProducto(new Producto());
        lote.setStockLote(0);
        lote.setEstado(EstadoLote.AGOTADO);
        lote.setFechaVencimiento(LocalDate.now().plusDays(10));
        detalle.setIdDetalleVenta(3L);
        detalle.setLote(lote);
        detalle.setCantidad(new BigDecimal("2"));
        detalle.setPrecioUnitario(new BigDecimal("10"));
        detalle.setEstado(EstadoDetalleVenta.V);
        when(em.find(Venta.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(venta);
        when(em.find(Lote.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(lote);
        when(detalles.findByVenta_idVenta(1L)).thenReturn(List.of(detalle));
        when(usuarios.findById(1L)).thenReturn(Optional.of(new Usuario()));
    }

    @Test void devolucionTotalReponeStockRegistraMovimientoYConservaImporteOriginal() {
        var respuesta = service.devolver(1L, new DevolucionVentaRequestDTO(1L, List.of(3L)));
        assertEquals(2, respuesta.lotes().getFirst().stockActual());
        assertEquals(EstadoLote.ACTIVO, lote.getEstado());
        assertEquals(EstadoDetalleVenta.D, detalle.getEstado());
        assertEquals(EstadoVenta.ANULADA, venta.getEstado());
        assertEquals(new BigDecimal("20"), venta.getTotal());
        assertEquals(new BigDecimal("20"), respuesta.importeDevuelto());
        var movimiento = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(em).persist(movimiento.capture());
        assertEquals(BigDecimal.ZERO, movimiento.getValue().getStockAnterior());
        assertEquals(new BigDecimal("2"), movimiento.getValue().getStockNuevo());
        assertThrows(ResponseStatusException.class,
                () -> service.devolver(1L, new DevolucionVentaRequestDTO(1L, List.of(3L))));
        assertEquals(2, lote.getStockLote());
    }

    @Test void seleccionParcialNoModificaLosOtrosDetalles() {
        DetalleVenta otro = new DetalleVenta();
        otro.setIdDetalleVenta(4L);
        otro.setEstado(EstadoDetalleVenta.V);
        when(detalles.findByVenta_idVenta(1L)).thenReturn(List.of(detalle, otro));
        service.devolver(1L, new DevolucionVentaRequestDTO(1L, List.of(3L)));
        assertEquals(EstadoDetalleVenta.V, otro.getEstado());
        assertEquals(EstadoVenta.EMITIDA, venta.getEstado());
        assertThrows(ResponseStatusException.class,
                () -> service.devolver(1L, new DevolucionVentaRequestDTO(1L, List.of(3L))));
    }

    @Test void rechazaDetalleAjenoYDuplicado() {
        assertThrows(ResponseStatusException.class,
                () -> service.devolver(1L, new DevolucionVentaRequestDTO(1L, List.of(9L))));
        assertThrows(ResponseStatusException.class,
                () -> service.devolver(1L, new DevolucionVentaRequestDTO(1L, List.of(3L, 3L))));
        assertEquals(0, lote.getStockLote());
        verify(em, never()).persist(any());
    }

    @Test void noReactivaUnLoteVencido() {
        lote.setFechaVencimiento(LocalDate.now().minusDays(1));
        service.devolver(1L, new DevolucionVentaRequestDTO(1L, List.of(3L)));
        assertEquals(EstadoLote.VENCIDO, lote.getEstado());
        assertEquals(2, lote.getStockLote());
    }

    @Test void rechazaCantidadesFraccionariasSinTruncarStock() {
        detalle.setCantidad(new BigDecimal("1.5"));
        assertThrows(ResponseStatusException.class,
                () -> service.devolver(1L, new DevolucionVentaRequestDTO(1L, List.of(3L))));
        assertEquals(0, lote.getStockLote());
    }
}
