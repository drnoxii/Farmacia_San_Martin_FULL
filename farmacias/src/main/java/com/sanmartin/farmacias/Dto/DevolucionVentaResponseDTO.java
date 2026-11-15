package com.sanmartin.farmacias.Dto;

import java.math.BigDecimal;
import java.util.List;

public record DevolucionVentaResponseDTO(
        Long idVenta, BigDecimal importeDevuelto, List<StockRepuesto> lotes) {
    public record StockRepuesto(Long idLote, String producto, String numeroLote, int stockActual) {}
}
