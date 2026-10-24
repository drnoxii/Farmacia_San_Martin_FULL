package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPersona", nullable = false, unique = true)
    private Persona persona;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "password", nullable = false, length = 120)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoGeneral estadoGeneral;

    public Usuario() {
    }

    public Usuario(Long idUsuario, Persona persona, String correo, String password, Rol rol, LocalDateTime fechaCreacion, EstadoGeneral estadoGeneral) {
        this.idUsuario = idUsuario;
        this.persona = persona;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
        this.fechaCreacion = fechaCreacion;
        this.estadoGeneral = estadoGeneral;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public EstadoGeneral getEstadoGeneral() {
        return estadoGeneral;
    }

    public void setEstadoGeneral(EstadoGeneral estadoGeneral) {
        this.estadoGeneral = estadoGeneral;
    }
}
