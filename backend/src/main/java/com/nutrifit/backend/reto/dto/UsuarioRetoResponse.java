package com.nutrifit.backend.reto.dto;

import java.time.LocalDateTime;

/**
 * DTO con el estado del progreso de un usuario en un reto.
 */
public class UsuarioRetoResponse {
    private Long id;
    private Long usuarioId;
    private Long retoId;
    private int progreso;
    private boolean completado;
    private LocalDateTime fechaCompletado;

    public UsuarioRetoResponse() {}

    public UsuarioRetoResponse(Long id, Long usuarioId, Long retoId, int progreso, boolean completado, LocalDateTime fechaCompletado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.retoId = retoId;
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

    public int getProgreso() { return progreso; }
    public void setProgreso(int progreso) { this.progreso = progreso; }

    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }

    public LocalDateTime getFechaCompletado() { return fechaCompletado; }
    public void setFechaCompletado(LocalDateTime fechaCompletado) { this.fechaCompletado = fechaCompletado; }
}
