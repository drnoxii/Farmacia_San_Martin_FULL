package com.sanmartin.farmacias.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPersona", nullable = false, unique = true)
    private Persona persona;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoGeneral estadoGeneral;

    public Cliente() {
    }

    public Cliente(Long idCliente, Persona persona, LocalDateTime fechaRegistro, EstadoGeneral estadoGeneral) {
        this.idCliente = idCliente;
        this.persona = persona;
        this.fechaRegistro = fechaRegistro;
        this.estadoGeneral = estadoGeneral;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public EstadoGeneral getEstadoGeneral() {
        return estadoGeneral;
    }

    public void setEstadoGeneral(EstadoGeneral estadoGeneral) {
        this.estadoGeneral = estadoGeneral;
    }
}
