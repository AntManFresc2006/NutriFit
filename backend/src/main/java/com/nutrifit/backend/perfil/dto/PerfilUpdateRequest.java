package com.nutrifit.backend.perfil.dto;

import com.nutrifit.backend.perfil.model.NivelActividad;
import com.nutrifit.backend.perfil.model.Sexo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/**
 * DTO de entrada para actualizar el perfil biométrico y de objetivos del usuario.
 */
public class PerfilUpdateRequest {

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @Min(value = 100, message = "La altura mínima es 100 cm")
    @Max(value = 250, message = "La altura máxima es 250 cm")
    private int alturaCm;

    @DecimalMin(value = "20.0", message = "El peso actual debe ser al menos 20 kg")
    private double pesoKgActual;

    private Double pesoObjetivo;

    @NotNull(message = "El nivel de actividad es obligatorio")
    private NivelActividad nivelActividad;

    public PerfilUpdateRequest() {
    }

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
}
