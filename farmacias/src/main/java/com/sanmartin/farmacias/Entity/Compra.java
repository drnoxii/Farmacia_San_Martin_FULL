package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Compra")
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idProveedor", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra = LocalDateTime.now();

    @Column(nullable = false)
    private BigDecimal totalCompra;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCompra estado = EstadoCompra.RECIBIDA;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> detalles = new ArrayList<>();

    public Compra() {
    }

    public Compra(Long idCompra, Proveedor proveedor, Usuario usuario, LocalDateTime fechaCompra, BigDecimal totalCompra, BigDecimal subtotal, EstadoCompra estado, List<DetalleCompra> detalles) {
        this.idCompra = idCompra;
        this.proveedor = proveedor;
        this.usuario = usuario;
        this.fechaCompra = fechaCompra;
        this.totalCompra = totalCompra;
        this.subtotal = subtotal;
        this.estado = estado;
        this.detalles = detalles;
    }

    public Long getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Long idCompra) {
        this.idCompra = idCompra;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public BigDecimal getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(BigDecimal totalCompra) {
        this.totalCompra = totalCompra;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public void setEstado(EstadoCompra estado) {
        this.estado = estado;
    }

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }
}
