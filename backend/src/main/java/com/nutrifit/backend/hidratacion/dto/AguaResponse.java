package com.nutrifit.backend.hidratacion.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AguaResponse {

    private Long id;
    private LocalDate fecha;
    private int cantidadMl;
    private LocalTime hora;

    public AguaResponse() {}

    public AguaResponse(Long id, LocalDate fecha, int cantidadMl, LocalTime hora) {
        this.id = id;
        this.fecha = fecha;
        this.cantidadMl = cantidadMl;
        this.hora = hora;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getCantidadMl() { return cantidadMl; }
    public void setCantidadMl(int cantidadMl) { this.cantidadMl = cantidadMl; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
}
