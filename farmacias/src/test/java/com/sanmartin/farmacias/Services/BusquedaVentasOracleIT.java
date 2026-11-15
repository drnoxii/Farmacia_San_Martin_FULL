package com.sanmartin.farmacias.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.jpa.hibernate.ddl-auto=none", "spring.jpa.show-sql=false"})
@EnabledIfEnvironmentVariable(named = "RUN_ORACLE_SEARCH_TESTS", matches = "true")
@Transactional(readOnly = true)
class BusquedaVentasOracleIT {
    @Autowired ReporteService servicio;
    private final LocalDate desde = LocalDate.of(2000, 1, 1);
    private final LocalDate hasta = LocalDate.of(2100, 1, 1);

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> filas(Map<String, Object> respuesta) {
        return (List<Map<String, Object>>) respuesta.get("ventas");
    }

    @Test void resumenCoincideConExportacionSinEnviarListaCompleta() {
        var completo = servicio.reporteVentas(desde, hasta);
        var resumen = servicio.resumenVentas(desde, hasta);
        assertFalse(resumen.containsKey("ventas"));
        assertEquals(filas(completo).size(), ((Number) resumen.get("totalRegistros")).intValue());
        for (String campo : List.of("totalVendido", "cantidadVentas", "totalAnulado", "cantidadAnuladas")) {
            assertEquals(0, new BigDecimal(completo.get(campo).toString())
                    .compareTo(new BigDecimal(resumen.get(campo).toString())), campo);
        }
    }

    @Test void paginasNoRepitenVentasYExportacionConservaTodas() {
        var primera = servicio.detalleVentas(desde, hasta, "", 0, 1);
        long total = ((Number) primera.get("totalRegistros")).longValue();
        assumeTrue(total >= 2);
        var segunda = servicio.detalleVentas(desde, hasta, "", 1, 1);
        assertEquals(1, filas(primera).size());
        assertEquals(1, filas(segunda).size());
        assertNotEquals(filas(primera).getFirst().get("idVenta"), filas(segunda).getFirst().get("idVenta"));
        assertEquals(total, filas(servicio.reporteVentas(desde, hasta)).size());
        var ultima = servicio.detalleVentas(desde, hasta, "", Integer.MAX_VALUE, 1);
        assertEquals(total - 1, ((Number) ultima.get("pagina")).longValue());
        assertEquals(1, filas(ultima).size());
    }

    @Test void buscaNumeroYClienteSinDistinguirMayusculasDentroDeFecha() {
        var todas = filas(servicio.reporteVentas(desde, hasta));
        assumeTrue(!todas.isEmpty());
        var venta = todas.getFirst();
        var fecha = LocalDate.parse(venta.get("fecha").toString().substring(0, 10));
        for (String campo : List.of("numeroVenta", "cliente")) {
            String busqueda = venta.get(campo).toString().toLowerCase(java.util.Locale.ROOT);
            var pagina = servicio.detalleVentas(fecha, fecha, " " + busqueda + " ", 0, 100);
            assertTrue(filas(pagina).stream().anyMatch(f -> f.get("idVenta").equals(venta.get("idVenta"))));
            assertTrue(filas(pagina).stream().allMatch(f -> f.get("fecha").toString().startsWith(fecha.toString())));
        }
    }

    @Test void textoEspecialNoSeInterpretaComoSqlNiComodin() {
        var todas = filas(servicio.reporteVentas(desde, hasta));
        for (String texto : List.of("%", "_", "' OR 1=1 --", "sin-coincidencia-zzzzzz")) {
            long esperado = todas.stream().filter(v -> v.get("numeroVenta").toString().contains(texto)
                    || v.get("cliente").toString().contains(texto)).count();
            var resultado = servicio.detalleVentas(desde, hasta, texto, 0, 20);
            assertEquals(esperado, ((Number) resultado.get("totalRegistros")).longValue());
            if (esperado == 0) {
                assertTrue(filas(resultado).isEmpty());
                assertEquals(0L, resultado.get("totalPaginas"));
                assertEquals(0, resultado.get("pagina"));
            }
        }
    }

    @Test void validaLimitesDePaginacionYFechas() {
        assertThrows(ResponseStatusException.class, () -> servicio.detalleVentas(desde, hasta, "", -1, 20));
        assertThrows(ResponseStatusException.class, () -> servicio.detalleVentas(desde, hasta, "", 0, 101));
        assertThrows(ResponseStatusException.class, () -> servicio.detalleVentas(desde, hasta, "", 0, 0));
        assertThrows(ResponseStatusException.class, () -> servicio.detalleVentas(hasta, desde, "", 0, 20));
        assertThrows(ResponseStatusException.class, () -> servicio.detalleVentas(desde, hasta, "x".repeat(101), 0, 20));
    }
}
