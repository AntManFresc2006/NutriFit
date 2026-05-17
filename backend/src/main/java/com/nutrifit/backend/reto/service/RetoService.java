package com.nutrifit.backend.reto.service;

import com.nutrifit.backend.reto.dto.AceptarRetoRequest;
import com.nutrifit.backend.reto.dto.RetoResponse;

import java.time.LocalDate;
import java.util.List;

public interface RetoService {
    List<RetoResponse> getRetos(Long usuarioId);
    RetoResponse aceptarReto(Long usuarioId, AceptarRetoRequest req);
    List<RetoResponse> sincronizarProgreso(Long usuarioId, LocalDate fecha);
    void abandonarReto(Long usuarioId, Long usuarioRetoId);
}
