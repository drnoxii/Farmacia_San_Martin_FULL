
package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Services.ReporteService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // ==========================================
    // 1. REPORTE DE VENTAS MEDIANTE SQL
    // ==========================================

    @GetMapping("/ventas")
    public Map<String, Object> ventas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta
    ) {

        validarFechas(desde, hasta);

        return reporteService.reporteVentas(
                desde,
                hasta
        );
    }

    // ==========================================
    // 2. PROCEDIMIENTO ALMACENADO DE ORACLE
    // ==========================================

    @GetMapping("/ventas/resumen-sp")
    public Map<String, Object> resumenDesdeProcedimiento(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta
    ) {

        validarFechas(desde, hasta);

        return reporteService.ejecutarResumenVentasSP(
                desde,
                hasta
        );
    }

    // ==========================================
    // 3. REPORTE SQL DE INVENTARIO
    // ==========================================

    @GetMapping("/inventario")
    public Map<String, Object> inventario(
            @RequestParam(defaultValue = "30") int dias
    ) {

        if (dias < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los días no pueden ser negativos"
            );
        }

        return reporteService.reporteInventario(dias);
    }

    // ==========================================
    // 4. REPORTE SQL DE CIERRES DE CAJA
    // ==========================================

    @GetMapping("/cierres-caja")
    public Map<String, Object> cierresCaja(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta
    ) {

        validarFechas(desde, hasta);

        return reporteService.reporteCierres(
                desde,
                hasta
        );
    }


    // ==========================================
    // 5. VALIDAR FECHAS
    // ==========================================

    private void validarFechas(
            LocalDate desde,
            LocalDate hasta
    ) {

        if (desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha desde no puede ser mayor que hasta"
            );
        }
    }

}
