package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    @Column(nullable = false, length = 50)
    private String nombreCategoria;

    @Column(nullable = false)
    private String descripcionCate;

    public Categoria() {
    }

    public Categoria(Long idCategoria, String nombreCategoria, String descripcionCate) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.descripcionCate = descripcionCate;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getDescripcionCate() {
        return descripcionCate;
    }

    public void setDescripcionCate(String descripcionCate) {
        this.descripcionCate = descripcionCate;
    }
}
