
package com.sanmartin.farmacias.Services;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private final JdbcTemplate jdbc;

    public ReporteService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String VENTAS_PERIODO = """
        SELECT V.ID_VENTA AS "idVenta", V.NUMERO_VENTA AS "numeroVenta",
               TO_CHAR(V.FECHA_VENTA, 'YYYY-MM-DD"T"HH24:MI:SS') AS "fecha",
               NVL(PC.NOMBRE, 'Público general') AS "cliente",
               NVL(PU.NOMBRE, 'Sin vendedor') AS "vendedor",
               V.TIPO_COMPROBANTE AS "tipoComprobante", V.METODO_PAGO AS "metodoPago",
               NVL(V.TOTAL, 0) AS "total", V.ESTADO AS "estado",
               NVL((SELECT SUM(D.CANTIDAD * D.PRECIO_UNITARIO)
                    FROM DETALLE_VENTA D
                    WHERE D.ID_VENTA = V.ID_VENTA AND D.ESTADO = 'D'), 0) AS "totalDevuelto"
        FROM VENTA V
        LEFT JOIN CLIENTE C ON C.ID_CLIENTE = V.ID_CLIENTE
        LEFT JOIN PERSONA PC ON PC.ID_PERSONA = C.ID_PERSONA
        LEFT JOIN USUARIO U ON U.ID_USUARIO = V.ID_USUARIO
        LEFT JOIN PERSONA PU ON PU.ID_PERSONA = U.ID_PERSONA
        WHERE V.FECHA_VENTA >= ? AND V.FECHA_VENTA < ?
        """;

    public Map<String, Object> detalleVentas(LocalDate desde, LocalDate hasta,
                                            String buscar, int pagina, int tamano) {
        if (desde.isAfter(hasta) || pagina < 0 || tamano < 1 || tamano > 100
                || buscar.length() > 100) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Filtros de búsqueda inválidos.");
        }
        var parametros = new ArrayList<Object>();
        parametros.add(Timestamp.valueOf(desde.atStartOfDay()));
        parametros.add(Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
        String consulta = " FROM (" + VENTAS_PERIODO + ") R";
        String texto = buscar.trim();
        if (!texto.isEmpty()) {
            // INSTR trata %, _ y comillas como texto, no como comodines SQL.
            consulta += " WHERE INSTR(UPPER(\"numeroVenta\"), UPPER(?)) > 0"
                    + " OR INSTR(UPPER(\"cliente\"), UPPER(?)) > 0";
            parametros.add(texto);
            parametros.add(texto);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*)" + consulta, Long.class, parametros.toArray());
        long totalRegistros = total == null ? 0 : total;
        long totalPaginas = (totalRegistros + tamano - 1) / tamano;
        int paginaActual = (int) Math.min(pagina, Math.max(0, totalPaginas - 1));
        parametros.add((long) paginaActual * tamano);
        parametros.add(tamano);
        var ventas = jdbc.queryForList("SELECT R.*" + consulta
                + " ORDER BY \"fecha\" DESC, \"idVenta\" DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                parametros.toArray());
        return Map.of("ventas", ventas, "totalRegistros", totalRegistros,
                "totalPaginas", totalPaginas, "pagina", paginaActual, "tamano", tamano);
    }

    public Map<String, Object> resumenVentas(LocalDate desde, LocalDate hasta) {
        Object[] fechas = {Timestamp.valueOf(desde.atStartOfDay()), Timestamp.valueOf(hasta.plusDays(1).atStartOfDay())};
        String fuente = " FROM (" + VENTAS_PERIODO + ") R";
        var respuesta = new LinkedHashMap<String, Object>(jdbc.queryForMap("""
            SELECT NVL(SUM(CASE WHEN "estado" = 'EMITIDA' THEN "total" - "totalDevuelto" ELSE 0 END), 0) AS "totalVendido",
                   COUNT(CASE WHEN "estado" = 'EMITIDA' THEN 1 END) AS "cantidadVentas",
                   NVL(SUM(CASE WHEN "estado" = 'ANULADA' THEN "total" ELSE 0 END), 0) AS "totalAnulado",
                   COUNT(CASE WHEN "estado" = 'ANULADA' THEN 1 END) AS "cantidadAnuladas",
                   COUNT(*) AS "totalRegistros"
            """ + fuente, fechas));
        // Los nombres de columna son constantes internas, nunca valores recibidos del cliente.
        for (var grupo : Map.of("porMetodoPago", "metodoPago", "porComprobante", "tipoComprobante",
                "porVendedor", "vendedor").entrySet()) {
            String campo = "NVL(\"" + grupo.getValue() + "\", 'Sin especificar')";
            respuesta.put(grupo.getKey(), jdbc.queryForList("SELECT " + campo
                    + " AS \"etiqueta\", COUNT(*) AS \"cantidad\", SUM(\"total\" - \"totalDevuelto\") AS \"total\""
                    + fuente + " WHERE \"estado\" = 'EMITIDA' GROUP BY " + campo + " ORDER BY " + campo, fechas));
        }
        respuesta.put("desde", desde.toString());
        respuesta.put("hasta", hasta.toString());
        return respuesta;
    }

    // ==========================================
    // 1. REPORTE DE VENTAS MEDIANTE SQL
    // ==========================================

    public Map<String, Object> reporteVentas(
            LocalDate desde, LocalDate hasta) {

        String sql = """
            SELECT
                V.ID_VENTA,
                V.NUMERO_VENTA,
                V.FECHA_VENTA,
                NVL(PC.NOMBRE, 'Público general') AS CLIENTE,
                NVL(PU.NOMBRE, 'Sin vendedor') AS VENDEDOR,
                V.TIPO_COMPROBANTE,
                V.METODO_PAGO,
                V.TOTAL,
                NVL((SELECT SUM(D.CANTIDAD * D.PRECIO_UNITARIO)
                     FROM DETALLE_VENTA D
                     WHERE D.ID_VENTA = V.ID_VENTA AND D.ESTADO = 'D'), 0) AS DEVUELTO,
                V.ESTADO
            FROM VENTA V
            LEFT JOIN CLIENTE C
                ON V.ID_CLIENTE = C.ID_CLIENTE
            LEFT JOIN PERSONA PC
                ON C.ID_PERSONA = PC.ID_PERSONA
            LEFT JOIN USUARIO U
                ON V.ID_USUARIO = U.ID_USUARIO
            LEFT JOIN PERSONA PU
                ON U.ID_PERSONA = PU.ID_PERSONA
            WHERE V.FECHA_VENTA >= ?
              AND V.FECHA_VENTA < ?
            ORDER BY V.FECHA_VENTA DESC
            """;

        List<Map<String, Object>> registros =
                jdbc.queryForList(
                        sql,
                        Timestamp.valueOf(desde.atStartOfDay()),
                        Timestamp.valueOf(
                                hasta.plusDays(1).atStartOfDay())
                );

        List<Map<String, Object>> ventas = new ArrayList<>();

        int cantidadVentas = 0;
        int cantidadAnuladas = 0;

        BigDecimal totalVendido = BigDecimal.ZERO;
        BigDecimal totalAnulado = BigDecimal.ZERO;

        for (Map<String, Object> r : registros) {

            BigDecimal total = r.get("TOTAL") == null
                    ? BigDecimal.ZERO
                    : new BigDecimal(r.get("TOTAL").toString());

            String estado = String.valueOf(r.get("ESTADO"));
            BigDecimal devuelto = new BigDecimal(r.get("DEVUELTO").toString());

            if ("ANULADA".equalsIgnoreCase(estado)) {
                cantidadAnuladas++;
                totalAnulado = totalAnulado.add(total);

            } else if ("EMITIDA".equalsIgnoreCase(estado)) {
                cantidadVentas++;
                totalVendido = totalVendido.add(total.subtract(devuelto));
            }

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("idVenta", r.get("ID_VENTA"));
            fila.put("totalDevuelto", devuelto);

            fila.put("numeroVenta", r.get("NUMERO_VENTA"));

            fila.put(
                    "fecha",
                    r.get("FECHA_VENTA") == null
                            ? null
                            : r.get("FECHA_VENTA").toString()
            );

            fila.put("cliente", r.get("CLIENTE"));
            fila.put("vendedor", r.get("VENDEDOR"));
            fila.put("tipoComprobante", r.get("TIPO_COMPROBANTE"));
            fila.put("metodoPago", r.get("METODO_PAGO"));
            fila.put("total", total);
            fila.put("estado", estado);

            ventas.add(fila);
        }

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put("desde", desde.toString());
        respuesta.put("hasta", hasta.toString());
        respuesta.put("totalVendido", totalVendido);
        respuesta.put("cantidadVentas", cantidadVentas);
        respuesta.put("cantidadAnuladas", cantidadAnuladas);
        respuesta.put("totalAnulado", totalAnulado);

        respuesta.put(
                "porMetodoPago",
                agrupar(ventas, "metodoPago")
        );

        respuesta.put(
                "porComprobante",
                agrupar(ventas, "tipoComprobante")
        );

        respuesta.put(
                "porVendedor",
                agrupar(ventas, "vendedor")
        );

        respuesta.put("ventas", ventas);

        return respuesta;
    }

    // ==========================================
    // 2. AGRUPAR LOS DATOS DEL REPORTE
    // ==========================================

    private List<Map<String, Object>> agrupar(
            List<Map<String, Object>> ventas,
            String campo) {

        Map<String, Integer> cantidades =
                new LinkedHashMap<>();

        Map<String, BigDecimal> totales =
                new LinkedHashMap<>();

        for (Map<String, Object> venta : ventas) {

            if (!"EMITIDA".equalsIgnoreCase(
                    String.valueOf(venta.get("estado")))) {
                continue;
            }

            String etiqueta = venta.get(campo) == null
                    ? "Sin especificar"
                    : venta.get(campo).toString();

            BigDecimal monto =
                    ((BigDecimal) venta.get("total")).subtract((BigDecimal) venta.get("totalDevuelto"));

            cantidades.merge(
                    etiqueta,
                    1,
                    Integer::sum
            );

            totales.merge(
                    etiqueta,
                    monto,
                    BigDecimal::add
            );
        }

        List<Map<String, Object>> resultado =
                new ArrayList<>();

        for (String etiqueta : cantidades.keySet()) {

            Map<String, Object> fila =
                    new LinkedHashMap<>();

            fila.put("etiqueta", etiqueta);
            fila.put("cantidad", cantidades.get(etiqueta));
            fila.put("total", totales.get(etiqueta));

            resultado.add(fila);
        }

        return resultado;
    }

    // ==========================================
    // 3. LLAMAR PROCEDIMIENTO ALMACENADO ORACLE
    // ==========================================

    public Map<String, Object> ejecutarResumenVentasSP(
            LocalDate desde,
            LocalDate hasta) {

        return jdbc.execute(
                (ConnectionCallback<Map<String, Object>>) conexion -> {

                    String llamada =
                            "{call SP_RESUMEN_VENTAS(?, ?, ?, ?)}";

                    try (CallableStatement cs =
                                 conexion.prepareCall(llamada)) {

                        // Parámetros de entrada
                        cs.setDate(
                                1,
                                java.sql.Date.valueOf(desde)
                        );

                        cs.setDate(
                                2,
                                java.sql.Date.valueOf(hasta)
                        );

                        // Parámetros de salida
                        cs.registerOutParameter(3, Types.NUMERIC);
                        cs.registerOutParameter(4, Types.NUMERIC);

                        // Ejecutar procedimiento en Oracle
                        cs.execute();

                        int cantidad = cs.getInt(3);
                        BigDecimal total = cs.getBigDecimal(4);

                        Map<String, Object> resultado =
                                new LinkedHashMap<>();

                        resultado.put(
                                "cantidadRegistros",
                                cantidad
                        );

                        resultado.put(
                                "totalRegistrado",
                                total == null
                                        ? BigDecimal.ZERO
                                        : total
                        );

                        resultado.put(
                                "origen",
                                "PROCEDIMIENTO_ALMACENADO"
                        );

                        return resultado;
                    }
                }
        );
    }

    // ==========================================
    // 4. REPORTE SQL DE INVENTARIO
    // ==========================================

    public Map<String, Object> reporteInventario(int dias) {

        if (dias < 0) {
            throw new IllegalArgumentException(
                    "Los días no pueden ser negativos"
            );
        }

        // --------------------------------------
        // A. PRODUCTOS CON STOCK BAJO
        // --------------------------------------

        String sqlStock = """
            SELECT
                CODIGO AS "codigo",
                PRODUCTO AS "producto",
                CATEGORIA AS "categoria",
                STOCK_ACTUAL AS "stockActual",
                STOCK_MINIMO AS "stockMinimo",
                (STOCK_MINIMO - STOCK_ACTUAL) AS "faltante"
            FROM (
                SELECT
                    P.CODIGO,
                    P.NOMBRE_PRODUCTO AS PRODUCTO,
                    C.NOMBRE_CATEGORIA AS CATEGORIA,
                    NVL(P.STOCK_MINIMO, 0) AS STOCK_MINIMO,
                    NVL(SUM(
                        CASE
                            WHEN L.ESTADO = 'ACTIVO'
                             AND L.FECHA_VENCIMIENTO >= TRUNC(SYSDATE)
                            THEN L.STOCK_LOTE
                            ELSE 0
                        END
                    ), 0) AS STOCK_ACTUAL
                FROM PRODUCTO P
                LEFT JOIN CATEGORIA C
                    ON P.ID_CATEGORIA = C.ID_CATEGORIA
                LEFT JOIN LOTE L
                    ON P.ID_PRODUCTO = L.ID_PRODUCTO
                GROUP BY
                    P.CODIGO,
                    P.NOMBRE_PRODUCTO,
                    C.NOMBRE_CATEGORIA,
                    P.STOCK_MINIMO
            )
            WHERE STOCK_ACTUAL < STOCK_MINIMO
            ORDER BY PRODUCTO
            """;

        List<Map<String, Object>> stockBajo =
                jdbc.queryForList(sqlStock);

        // --------------------------------------
        // B. LOTES PROXIMOS A VENCER
        // --------------------------------------

        String sqlPorVencer = """
            SELECT
                P.CODIGO AS "codigo",
                P.NOMBRE_PRODUCTO AS "producto",
                L.NUMERO_LOTE AS "numeroLote",
                TO_CHAR(
                    L.FECHA_VENCIMIENTO,
                    'YYYY-MM-DD'
                ) AS "fechaVencimiento",
                L.STOCK_LOTE AS "stock",
                TRUNC(L.FECHA_VENCIMIENTO)
                  - TRUNC(SYSDATE) AS "diasRestantes"
            FROM LOTE L
            JOIN PRODUCTO P
                ON L.ID_PRODUCTO = P.ID_PRODUCTO
            WHERE L.STOCK_LOTE > 0
              AND L.ESTADO = 'ACTIVO'
              AND L.FECHA_VENCIMIENTO >= TRUNC(SYSDATE)
              AND L.FECHA_VENCIMIENTO < TRUNC(SYSDATE) + ? + 1
            ORDER BY L.FECHA_VENCIMIENTO
            """;

        List<Map<String, Object>> lotesPorVencer =
                jdbc.queryForList(sqlPorVencer, dias);

        // --------------------------------------
        // C. LOTES VENCIDOS CON STOCK
        // --------------------------------------

        String sqlVencidos = """
            SELECT
                P.CODIGO AS "codigo",
                P.NOMBRE_PRODUCTO AS "producto",
                L.NUMERO_LOTE AS "numeroLote",
                TO_CHAR(
                    L.FECHA_VENCIMIENTO,
                    'YYYY-MM-DD'
                ) AS "fechaVencimiento",
                L.STOCK_LOTE AS "stock",
                TRUNC(L.FECHA_VENCIMIENTO)
                  - TRUNC(SYSDATE) AS "diasRestantes"
            FROM LOTE L
            JOIN PRODUCTO P
                ON L.ID_PRODUCTO = P.ID_PRODUCTO
            WHERE L.STOCK_LOTE > 0
              AND L.FECHA_VENCIMIENTO < TRUNC(SYSDATE)
            ORDER BY L.FECHA_VENCIMIENTO
            """;

        List<Map<String, Object>> lotesVencidos =
                jdbc.queryForList(sqlVencidos);

        // --------------------------------------
        // D. RESPUESTA PARA ANGULAR
        // --------------------------------------

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put(
                "fecha",
                jdbc.queryForObject(
                        "SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD') FROM DUAL",
                        String.class
                )
        );

        respuesta.put("dias", dias);
        respuesta.put("stockBajo", stockBajo);
        respuesta.put("lotesPorVencer", lotesPorVencer);
        respuesta.put("lotesVencidos", lotesVencidos);

        return respuesta;
    }

    // ==========================================
    // 5. REPORTE SQL DE CIERRES DE CAJA
    // ==========================================

    public Map<String, Object> reporteCierres(
            LocalDate desde,
            LocalDate hasta) {

        String sql = """
            SELECT
                C.ID_CAJA AS "idCaja",

                NVL(P.NOMBRE, 'Sin cajero') AS "cajero",

                TO_CHAR(
                    C.FECHA_APERTURA,
                    'YYYY-MM-DD"T"HH24:MI:SS'
                ) AS "apertura",

                TO_CHAR(
                    C.FECHA_CIERRE,
                    'YYYY-MM-DD"T"HH24:MI:SS'
                ) AS "cierre",

                NVL(C.MONTO_INICIAL, 0) AS "montoInicial",

                NVL(C.MONTO_FINAL - C.DIFERENCIA - NVL(C.MONTO_INICIAL, 0),
                    NVL(V.VENTAS_EFECTIVO, 0)) AS "ventasEfectivo",

                NVL(C.MONTO_FINAL - C.DIFERENCIA, (
                    NVL(C.MONTO_INICIAL, 0) +
                    NVL(V.VENTAS_EFECTIVO, 0)
                )) AS "esperado",

                NVL(C.MONTO_FINAL, 0) AS "contado",

                C.DIFERENCIA AS "diferencia"

            FROM CAJA C

            LEFT JOIN USUARIO U
                ON C.ID_USUARIO = U.ID_USUARIO

            LEFT JOIN PERSONA P
                ON U.ID_PERSONA = P.ID_PERSONA

            LEFT JOIN (
                SELECT
                    ID_CAJA,

                    SUM(
                        CASE
                            WHEN UPPER(TRIM(METODO_PAGO)) = 'EFECTIVO'
                             AND UPPER(TRIM(ESTADO)) = 'EMITIDA'
                            THEN NVL(TOTAL, 0) - NVL(DV.DEVUELTO, 0)
                            ELSE 0
                        END
                    ) AS VENTAS_EFECTIVO

                FROM VENTA VT
                LEFT JOIN (
                    SELECT ID_VENTA, SUM(CANTIDAD * PRECIO_UNITARIO) AS DEVUELTO
                    FROM DETALLE_VENTA WHERE ESTADO = 'D'
                    GROUP BY ID_VENTA
                ) DV ON DV.ID_VENTA = VT.ID_VENTA
                GROUP BY ID_CAJA

            ) V ON C.ID_CAJA = V.ID_CAJA

            WHERE C.FECHA_CIERRE >= ?
              AND C.FECHA_CIERRE < ?
              AND C.ESTADO = 'CERRADA'

            ORDER BY C.FECHA_CIERRE DESC
            """;

        List<Map<String, Object>> cierres =
                jdbc.queryForList(
                        sql,
                        Timestamp.valueOf(
                                desde.atStartOfDay()
                        ),
                        Timestamp.valueOf(
                                hasta.plusDays(1).atStartOfDay()
                        )
                );

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        BigDecimal totalSobrantes = BigDecimal.ZERO;
        BigDecimal totalFaltantes = BigDecimal.ZERO;

        for (Map<String, Object> cierre : cierres) {
            Object valor = cierre.get("diferencia");

            BigDecimal diferencia = valor == null
                    ? BigDecimal.ZERO
                    : new BigDecimal(valor.toString());

            if (diferencia.signum() > 0) {
                totalSobrantes = totalSobrantes.add(diferencia);
            } else if (diferencia.signum() < 0) {
                totalFaltantes = totalFaltantes.add(diferencia.abs());
            }
        }

        respuesta.put("desde", desde.toString());
        respuesta.put("hasta", hasta.toString());
        respuesta.put("cantidad", cierres.size());
        respuesta.put("totalSobrantes", totalSobrantes);
        respuesta.put("totalFaltantes", totalFaltantes);
        respuesta.put("cierres", cierres);

        return respuesta;
    }

}
