package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Detalle_Venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalleVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idVenta", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idLote", nullable = false)
    private Lote lote;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(
            name = "subtotal",
            precision = 12,
            scale = 2,
            insertable = false,
            updatable = false
    )
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private EstadoDetalleVenta estado;

    public DetalleVenta() {
    }

    public DetalleVenta(Long idDetalleVenta, Venta venta, Lote lote, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal, EstadoDetalleVenta estado) {
        this.idDetalleVenta = idDetalleVenta;
        this.venta = venta;
        this.lote = lote;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.estado = estado;
    }

    public Long getIdDetalleVenta() {
        return idDetalleVenta;
    }

    public void setIdDetalleVenta(Long idDetalleVenta) {
        this.idDetalleVenta = idDetalleVenta;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public EstadoDetalleVenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoDetalleVenta estado) {
        this.estado = estado;
    }
}
