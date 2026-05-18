package com.nutrifit.backend.hidratacion.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Modelo que representa el registro de una ingesta de agua por un usuario.
 */
public class AguaRegistro {

    private Long id;
    private Long usuarioId;
    private LocalDate fecha;
    private int cantidadMl;
    private LocalTime hora;

    public AguaRegistro() {}

    public AguaRegistro(Long id, Long usuarioId, LocalDate fecha, int cantidadMl, LocalTime hora) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.cantidadMl = cantidadMl;
        this.hora = hora;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getCantidadMl() { return cantidadMl; }
    public void setCantidadMl(int cantidadMl) { this.cantidadMl = cantidadMl; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
}
