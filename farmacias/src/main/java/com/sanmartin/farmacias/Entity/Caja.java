package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Caja")
public class Caja {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long idCaja;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "idUsuario", nullable = false)
        private Usuario usuario;

        @Column(name = "monto_inicial", nullable = false, precision = 10, scale = 2)
        private BigDecimal montoInicial;

        @Column(name = "monto_final", precision = 10, scale = 2)
        private BigDecimal montoFinal;

        @Column(precision = 10, scale = 2)
        private BigDecimal diferencia;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 15)
        private EstadoCaja estado = EstadoCaja.ABIERTA;

        @Column(name = "fecha_apertura", nullable = false)
        private LocalDateTime fechaApertura = LocalDateTime.now();

        @Column(name = "fecha_cierre")
        private LocalDateTime fechaCierre;

    public Caja() {
    }

    public Caja(Long idCaja, Usuario usuario, BigDecimal montoInicial, BigDecimal montoFinal, BigDecimal diferencia, EstadoCaja estado, LocalDateTime fechaApertura, LocalDateTime fechaCierre) {
        this.idCaja = idCaja;
        this.usuario = usuario;
        this.montoInicial = montoInicial;
        this.montoFinal = montoFinal;
        this.diferencia = diferencia;
        this.estado = estado;
        this.fechaApertura = fechaApertura;
        this.fechaCierre = fechaCierre;
    }

    public Long getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(Long idCaja) {
        this.idCaja = idCaja;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
    }

    public BigDecimal getMontoFinal() {
        return montoFinal;
    }

    public void setMontoFinal(BigDecimal montoFinal) {
        this.montoFinal = montoFinal;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public EstadoCaja getEstado() {
        return estado;
    }

    public void setEstado(EstadoCaja estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
}
