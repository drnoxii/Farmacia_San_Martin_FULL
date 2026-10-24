package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Proveedor")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProveedor;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(length = 15)
    private String telefono;

    @Column(length = 100)
    private String correo;

    @Column(length = 250)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    public Proveedor() {
    }

    public Proveedor(Long idProveedor, String razonSocial, String ruc, String telefono, String correo, String direccion, EstadoGeneral estado) {
        this.idProveedor = idProveedor;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.estado = estado;
    }

    public Long getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Long idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public EstadoGeneral getEstado() {
        return estado;
    }

    public void setEstado(EstadoGeneral estado) {
        this.estado = estado;
    }
}
