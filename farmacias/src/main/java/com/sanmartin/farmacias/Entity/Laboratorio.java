package com.sanmartin.farmacias.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Laboratorio")
public class Laboratorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLaboratorio;

    @Column(nullable = false, length = 50)
    private String nombreLaboratorio;

    @Column(nullable = false, length = 50)
    private String PaisOrigenLab;

    public Laboratorio() {
    }

    public Laboratorio(Long idLaboratorio, String nombreLaboratorio, String paisOrigenLab) {
        this.idLaboratorio = idLaboratorio;
        this.nombreLaboratorio = nombreLaboratorio;
        PaisOrigenLab = paisOrigenLab;
    }

    public Long getIdLaboratorio() {
        return idLaboratorio;
    }

    public void setIdLaboratorio(Long idLaboratorio) {
        this.idLaboratorio = idLaboratorio;
    }

    public String getNombreLaboratorio() {
        return nombreLaboratorio;
    }

    public void setNombreLaboratorio(String nombreLaboratorio) {
        this.nombreLaboratorio = nombreLaboratorio;
    }

    public String getPaisOrigenLab() {
        return PaisOrigenLab;
    }

    public void setPaisOrigenLab(String paisOrigenLab) {
        PaisOrigenLab = paisOrigenLab;
    }
}
