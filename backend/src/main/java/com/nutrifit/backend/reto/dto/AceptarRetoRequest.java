package com.nutrifit.backend.reto.dto;

import jakarta.validation.constraints.NotNull;

public class AceptarRetoRequest {
    @NotNull
    private Long retoId;

    public AceptarRetoRequest() {}

    public AceptarRetoRequest(Long retoId) {
        this.retoId = retoId;
    }

    public Long getRetoId() { return retoId; }
    public void setRetoId(Long retoId) { this.retoId = retoId; }
}
