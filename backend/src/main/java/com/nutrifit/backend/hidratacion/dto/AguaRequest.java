package com.nutrifit.backend.hidratacion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;

public class AguaRequest {

    @NotNull(message = "La fecha es requerida")
    private LocalDate fecha;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0 ml")
    @Max(value = 5000, message = "La cantidad debe ser menor a 5000 ml")
    private Integer cantidadMl;

    public AguaRequest() {}

    public AguaRequest(LocalDate fecha, Integer cantidadMl) {
        this.fecha = fecha;
        this.cantidadMl = cantidadMl;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Integer getCantidadMl() { return cantidadMl; }
    public void setCantidadMl(Integer cantidadMl) { this.cantidadMl = cantidadMl; }
}
