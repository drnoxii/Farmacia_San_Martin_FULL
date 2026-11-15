package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.DevolucionVentaRequestDTO;
import com.sanmartin.farmacias.Entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Prueba optativa en Oracle: todas las escrituras se revierten al terminar. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.jpa.hibernate.ddl-auto=none", "spring.jpa.show-sql=false"})
@EnabledIfEnvironmentVariable(named = "RUN_ORACLE_RETURN_TESTS", matches = "true")
@Transactional
@Rollback
class DevolucionOracleIT {
    @Autowired DevolucionVentaService devoluciones;
    @Autowired ReporteService reportes;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;

    @Test void persistenciaStockAuditoriaYReportesConRollback() {
        List<DetalleVenta> candidatos = em.createQuery(
                "select d from DetalleVenta d where d.estado = 'V' and d.venta.estado = 'EMITIDA' order by d.idDetalleVenta",
                DetalleVenta.class).setMaxResults(1).getResultList();
        assumeFalse(candidatos.isEmpty(), "Se necesita una venta emitida para la prueba con rollback.");
        DetalleVenta detalle = candidatos.getFirst();
        Venta venta = detalle.getVenta();
        Long idLote = detalle.getLote().getIdLote();
        int stockAnterior = detalle.getLote().getStockLote();
        int cantidad = detalle.getCantidad().intValueExact();
        Long movimientosAntes = jdbc.queryForObject("select count(*) from SAN_MARTIN.MOVIMIENTO_INVENTARIO", Long.class);
        var fecha = venta.getFechaVenta().toLocalDate();
        BigDecimal vendidoAntes = (BigDecimal) reportes.reporteVentas(fecha, fecha).get("totalVendido");
        var cierresAntes = reportes.reporteCierres(fecha, java.time.LocalDate.now());

        var resultado = devoluciones.devolver(venta.getIdVenta(),
                new DevolucionVentaRequestDTO(venta.getUsuario().getIdUsuario(), List.of(detalle.getIdDetalleVenta())));
        em.clear();
        assertEquals(stockAnterior + cantidad, em.find(Lote.class, idLote).getStockLote());
        assertEquals(EstadoDetalleVenta.D, em.find(DetalleVenta.class, detalle.getIdDetalleVenta()).getEstado());
        assertEquals(movimientosAntes + 1, jdbc.queryForObject("select count(*) from SAN_MARTIN.MOVIMIENTO_INVENTARIO", Long.class));
        BigDecimal vendidoDespues = (BigDecimal) reportes.reporteVentas(fecha, fecha).get("totalVendido");
        assertEquals(0, vendidoAntes.subtract(resultado.importeDevuelto()).compareTo(vendidoDespues));
        assertEquals(cierresAntes, reportes.reporteCierres(fecha, java.time.LocalDate.now()));
    }
}
