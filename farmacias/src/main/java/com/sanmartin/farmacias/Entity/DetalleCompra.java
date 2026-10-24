package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Detalle_Compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalleCompra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCompra", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idProducto", nullable = false)
    private Producto producto;

    @Column(nullable = false, precision = 10, scale = 2)
    private Integer cantidad;

    @Column(name = "precio_costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCosto;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;


    public DetalleCompra() {
    }

    public DetalleCompra(Long idDetalleCompra, Compra compra, Producto producto, Integer cantidad, BigDecimal precioCosto, BigDecimal subtotal) {
        this.idDetalleCompra = idDetalleCompra;
        this.compra = compra;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioCosto = precioCosto;
        this.subtotal = subtotal;
    }

    public Long getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(Long idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
