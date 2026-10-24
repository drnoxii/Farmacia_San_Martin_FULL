package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(nullable = false, length = 100)
    private String nombreProducto;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "idCategoria", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "idLaboratorio")
    private Laboratorio laboratorio;

    @Column(name = "precioCompra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "precioVenta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    private Integer stockMinimo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoGeneral estadoGeneral;

    public Producto() {
    }

    public Producto(Long idProducto, String nombreProducto, String descripcion, Categoria categoria, Laboratorio laboratorio, BigDecimal precioCompra, BigDecimal precioVenta, Integer stockMinimo, EstadoGeneral estadoGeneral) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.laboratorio = laboratorio;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stockMinimo = stockMinimo;
        this.estadoGeneral = estadoGeneral;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Laboratorio getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(Laboratorio laboratorio) {
        this.laboratorio = laboratorio;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public EstadoGeneral getEstadoGeneral() {
        return estadoGeneral;
    }

    public void setEstadoGeneral(EstadoGeneral estadoGeneral) {
        this.estadoGeneral = estadoGeneral;
    }
}
