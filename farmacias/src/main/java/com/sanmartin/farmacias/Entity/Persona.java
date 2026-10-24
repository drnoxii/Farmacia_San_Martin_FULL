package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Persona")
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPersona;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipoDocumento",
            length = 3,
            columnDefinition = "CHAR(3)"
    )
    private TipoDocumento tipoDocumento;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "numeroDocumento", length = 11, unique = true)
    private String numeroDocumento;

    private String telefono;
    private String direccion;

    public Persona() {
    }

    public Persona(Long idPersona, TipoDocumento tipoDocumento, String nombre, String numeroDocumento, String telefono, String direccion) {
        this.idPersona = idPersona;
        this.tipoDocumento = tipoDocumento;
        this.nombre = nombre;
        this.numeroDocumento = numeroDocumento;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
