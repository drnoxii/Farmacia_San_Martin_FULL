package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Lote")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProducto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDetalleCompra", nullable = false)
    private DetalleCompra detalleCompra;

    @Column(name = "numero_lote", nullable = false, length = 20)
    private String numeroLote;

    @Column(name = "stock_lote", nullable = false)
    private Integer stockLote = 0;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLote estado = EstadoLote.ACTIVO;

    public Lote() {
    }

    public Lote(Long idLote, Producto producto, DetalleCompra detalleCompra, String numeroLote, Integer stockLote, LocalDate fechaVencimiento, LocalDateTime fechaIngreso, EstadoLote estado) {
        this.idLote = idLote;
        this.producto = producto;
        this.detalleCompra = detalleCompra;
        this.numeroLote = numeroLote;
        this.stockLote = stockLote;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
    }

    public Long getIdLote() {
        return idLote;
    }

    public void setIdLote(Long idLote) {
        this.idLote = idLote;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public DetalleCompra getDetalleCompra() {
        return detalleCompra;
    }

    public void setDetalleCompra(DetalleCompra detalleCompra) {
        this.detalleCompra = detalleCompra;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(String numeroLote) {
        this.numeroLote = numeroLote;
    }

    public Integer getStockLote() {
        return stockLote;
    }

    public void setStockLote(Integer stockLote) {
        this.stockLote = stockLote;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public void setEstado(EstadoLote estado) {
        this.estado = estado;
    }
}
