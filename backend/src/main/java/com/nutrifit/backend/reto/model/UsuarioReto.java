package com.nutrifit.backend.reto.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UsuarioReto {
    private Long id;
    private Long usuarioId;
    private Long retoId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int progreso;
    private boolean completado;
    private LocalDateTime fechaCompletado;

    public UsuarioReto() {}

    public UsuarioReto(Long id, Long usuarioId, Long retoId, LocalDate fechaInicio, LocalDate fechaFin, int progreso, boolean completado, LocalDateTime fechaCompletado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.retoId = retoId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.progreso = progreso;
        this.completado = completado;
        this.fechaCompletado = fechaCompletado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getRetoId() { return retoId; }
    public void setRetoId(Long retoId) { this.retoId = retoId; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public int getProgreso() { return progreso; }
    public void setProgreso(int progreso) { this.progreso = progreso; }

    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }

    public LocalDateTime getFechaCompletado() { return fechaCompletado; }
    public void setFechaCompletado(LocalDateTime fechaCompletado) { this.fechaCompletado = fechaCompletado; }
}
