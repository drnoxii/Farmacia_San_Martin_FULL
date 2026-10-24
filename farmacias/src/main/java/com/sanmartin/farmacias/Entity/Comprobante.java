package com.sanmartin.farmacias.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Comprobante")
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idComprobante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idVenta", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idComprobanteReferencia")
    private Comprobante comprobanteReferencia;   // para notas de crédito/débito

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 15)
    private TipoComprobante tipoComprobante;

    @Column(nullable = false, length = 4)
    private String serie;

    @Column(name = "numero_correlativo", nullable = false, length = 8)
    private String numeroCorrelativo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 3)
    private String moneda = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sunat", nullable = false, length = 15)
    private EstadoSunat estadoSunat = EstadoSunat.PENDIENTE;

    @Column(name = "hash_cpe", length = 100)
    private String hashCpe;

    @Column(name = "ruta_xml", length = 255)
    private String rutaXml;

    @Column(name = "ruta_pdf", length = 255)
    private String rutaPdf;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    public Comprobante() {
    }

    public Comprobante(Long idComprobante, Venta venta, Comprobante comprobanteReferencia, TipoComprobante tipoComprobante, String serie, String numeroCorrelativo, BigDecimal subtotal, BigDecimal igv, BigDecimal total, String moneda, EstadoSunat estadoSunat, String hashCpe, String rutaXml, String rutaPdf, LocalDateTime fechaEmision) {
        this.idComprobante = idComprobante;
        this.venta = venta;
        this.comprobanteReferencia = comprobanteReferencia;
        this.tipoComprobante = tipoComprobante;
        this.serie = serie;
        this.numeroCorrelativo = numeroCorrelativo;
        this.subtotal = subtotal;
        this.igv = igv;
        this.total = total;
        this.moneda = moneda;
        this.estadoSunat = estadoSunat;
        this.hashCpe = hashCpe;
        this.rutaXml = rutaXml;
        this.rutaPdf = rutaPdf;
        this.fechaEmision = fechaEmision;
    }

    public Long getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(Long idComprobante) {
        this.idComprobante = idComprobante;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Comprobante getComprobanteReferencia() {
        return comprobanteReferencia;
    }

    public void setComprobanteReferencia(Comprobante comprobanteReferencia) {
        this.comprobanteReferencia = comprobanteReferencia;
    }

    public TipoComprobante getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(TipoComprobante tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getNumeroCorrelativo() {
        return numeroCorrelativo;
    }

    public void setNumeroCorrelativo(String numeroCorrelativo) {
        this.numeroCorrelativo = numeroCorrelativo;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public EstadoSunat getEstadoSunat() {
        return estadoSunat;
    }

    public void setEstadoSunat(EstadoSunat estadoSunat) {
        this.estadoSunat = estadoSunat;
    }

    public String getHashCpe() {
        return hashCpe;
    }

    public void setHashCpe(String hashCpe) {
        this.hashCpe = hashCpe;
    }

    public String getRutaXml() {
        return rutaXml;
    }

    public void setRutaXml(String rutaXml) {
        this.rutaXml = rutaXml;
    }

    public String getRutaPdf() {
        return rutaPdf;
    }

    public void setRutaPdf(String rutaPdf) {
        this.rutaPdf = rutaPdf;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}
