
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

    // ==========================================
    // 1. REPORTE DE VENTAS MEDIANTE SQL
    // ==========================================

    public Map<String, Object> reporteVentas(
            LocalDate desde, LocalDate hasta) {

        String sql = """
            SELECT
                V.NUMERO_VENTA,
                V.FECHA_VENTA,
                NVL(PC.NOMBRE, 'Público general') AS CLIENTE,
                NVL(PU.NOMBRE, 'Sin vendedor') AS VENDEDOR,
                V.TIPO_COMPROBANTE,
                V.METODO_PAGO,
                V.TOTAL,
                V.ESTADO
            FROM SAN_MARTIN.VENTA V
            LEFT JOIN SAN_MARTIN.CLIENTE C
                ON V.ID_CLIENTE = C.ID_CLIENTE
            LEFT JOIN SAN_MARTIN.PERSONA PC
                ON C.ID_PERSONA = PC.ID_PERSONA
            LEFT JOIN SAN_MARTIN.USUARIO U
                ON V.ID_USUARIO = U.ID_USUARIO
            LEFT JOIN SAN_MARTIN.PERSONA PU
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

            if ("ANULADA".equalsIgnoreCase(estado)) {
                cantidadAnuladas++;
                totalAnulado = totalAnulado.add(total);

            } else if ("EMITIDA".equalsIgnoreCase(estado)) {
                cantidadVentas++;
                totalVendido = totalVendido.add(total);
            }

            Map<String, Object> fila = new LinkedHashMap<>();

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
                    (BigDecimal) venta.get("total");

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
                            "{call SAN_MARTIN.SP_RESUMEN_VENTAS(?, ?, ?, ?)}";

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
                FROM SAN_MARTIN.PRODUCTO P
                LEFT JOIN SAN_MARTIN.CATEGORIA C
                    ON P.ID_CATEGORIA = C.ID_CATEGORIA
                LEFT JOIN SAN_MARTIN.LOTE L
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
            FROM SAN_MARTIN.LOTE L
            JOIN SAN_MARTIN.PRODUCTO P
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
            FROM SAN_MARTIN.LOTE L
            JOIN SAN_MARTIN.PRODUCTO P
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

                NVL(V.VENTAS_EFECTIVO, 0) AS "ventasEfectivo",

                (
                    NVL(C.MONTO_INICIAL, 0) +
                    NVL(V.VENTAS_EFECTIVO, 0)
                ) AS "esperado",

                NVL(C.MONTO_FINAL, 0) AS "contado",

                C.DIFERENCIA AS "diferencia"

            FROM SAN_MARTIN.CAJA C

            LEFT JOIN SAN_MARTIN.USUARIO U
                ON C.ID_USUARIO = U.ID_USUARIO

            LEFT JOIN SAN_MARTIN.PERSONA P
                ON U.ID_PERSONA = P.ID_PERSONA

            LEFT JOIN (
                SELECT
                    ID_CAJA,

                    SUM(
                        CASE
                            WHEN UPPER(TRIM(METODO_PAGO)) = 'EFECTIVO'
                             AND UPPER(TRIM(ESTADO)) = 'EMITIDA'
                            THEN NVL(TOTAL, 0)
                            ELSE 0
                        END
                    ) AS VENTAS_EFECTIVO

                FROM SAN_MARTIN.VENTA
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

        respuesta.put("desde", desde.toString());
        respuesta.put("hasta", hasta.toString());
        respuesta.put("cierres", cierres);

        return respuesta;
    }

}
