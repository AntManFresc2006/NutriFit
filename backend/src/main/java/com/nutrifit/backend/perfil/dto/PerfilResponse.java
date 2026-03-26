package com.nutrifit.backend.perfil.dto;

import com.nutrifit.backend.perfil.model.NivelActividad;
import com.nutrifit.backend.perfil.model.Sexo;

import java.time.LocalDate;

/**
 * DTO de salida con los datos de perfil del usuario y los valores calculados de TMB y TDEE.
 */
public class PerfilResponse {

    private Long id;
    private String nombre;
    private String email;
    private Sexo sexo;
    private LocalDate fechaNacimiento;
    private int alturaCm;
    private double pesoKgActual;
    private Double pesoObjetivo;
    private NivelActividad nivelActividad;
    private double tmb;
    private double tdee;

    public PerfilResponse() {
    }

    public PerfilResponse(Long id, String nombre, String email, Sexo sexo, LocalDate fechaNacimiento,
                          int alturaCm, double pesoKgActual, Double pesoObjetivo,
                          NivelActividad nivelActividad, double tmb, double tdee) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.alturaCm = alturaCm;
        this.pesoKgActual = pesoKgActual;
        this.pesoObjetivo = pesoObjetivo;
        this.nivelActividad = nivelActividad;
        this.tmb = tmb;
        this.tdee = tdee;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public int getAlturaCm() { return alturaCm; }
    public void setAlturaCm(int alturaCm) { this.alturaCm = alturaCm; }

    public double getPesoKgActual() { return pesoKgActual; }
    public void setPesoKgActual(double pesoKgActual) { this.pesoKgActual = pesoKgActual; }

    public Double getPesoObjetivo() { return pesoObjetivo; }
    public void setPesoObjetivo(Double pesoObjetivo) { this.pesoObjetivo = pesoObjetivo; }

    public NivelActividad getNivelActividad() { return nivelActividad; }
    public void setNivelActividad(NivelActividad nivelActividad) { this.nivelActividad = nivelActividad; }

    public double getTmb() { return tmb; }
    public void setTmb(double tmb) { this.tmb = tmb; }

    public double getTdee() { return tdee; }
    public void setTdee(double tdee) { this.tdee = tdee; }
}
