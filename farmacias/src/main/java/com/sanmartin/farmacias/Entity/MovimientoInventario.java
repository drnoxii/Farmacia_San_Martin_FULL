package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Movimiento_Inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMovimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idLote", nullable = false)
    private Lote lote;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimientoInventario tipoMovimiento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "stock_anterior", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockAnterior;

    @Column(name = "stock_nuevo", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockNuevo;

    @Enumerated(EnumType.STRING)
    @Column(name = "referencia_tipo", length = 25)
    private ReferenciaTipo referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    public MovimientoInventario() {
    }

    public MovimientoInventario(Long idMovimiento, Lote lote, TipoMovimientoInventario tipoMovimiento, BigDecimal cantidad, BigDecimal stockAnterior, BigDecimal stockNuevo, ReferenciaTipo referenciaTipo, Long referenciaId, LocalDateTime fecha, Usuario usuario) {
        this.idMovimiento = idMovimiento;
        this.lote = lote;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = stockNuevo;
        this.referenciaTipo = referenciaTipo;
        this.referenciaId = referenciaId;
        this.fecha = fecha;
        this.usuario = usuario;
    }

    public Long getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Long idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public TipoMovimientoInventario getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(TipoMovimientoInventario tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(BigDecimal stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public BigDecimal getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(BigDecimal stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public ReferenciaTipo getReferenciaTipo() {
        return referenciaTipo;
    }

    public void setReferenciaTipo(ReferenciaTipo referenciaTipo) {
        this.referenciaTipo = referenciaTipo;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
