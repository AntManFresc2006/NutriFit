package com.nutrifit.client.model;

/**
 * DTO que representa la respuesta del backend para el perfil del usuario.
 * Incluye los datos biométricos y los valores calculados de TMB y TDEE.
 */
public class PerfilDto {

    private Long id;
    private String nombre;
    private String email;
    private String sexo;
    private String fechaNacimiento;
    private int alturaCm;
    private double pesoKgActual;
    private Double pesoObjetivo;
    private String nivelActividad;
    private double tmb;
    private double tdee;

    public PerfilDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public int getAlturaCm() { return alturaCm; }
    public void setAlturaCm(int alturaCm) { this.alturaCm = alturaCm; }

    public double getPesoKgActual() { return pesoKgActual; }
    public void setPesoKgActual(double pesoKgActual) { this.pesoKgActual = pesoKgActual; }

    public Double getPesoObjetivo() { return pesoObjetivo; }
    public void setPesoObjetivo(Double pesoObjetivo) { this.pesoObjetivo = pesoObjetivo; }

    public String getNivelActividad() { return nivelActividad; }
    public void setNivelActividad(String nivelActividad) { this.nivelActividad = nivelActividad; }

    public double getTmb() { return tmb; }
    public void setTmb(double tmb) { this.tmb = tmb; }

    public double getTdee() { return tdee; }
    public void setTdee(double tdee) { this.tdee = tdee; }
}
