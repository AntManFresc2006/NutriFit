package com.nutrifit.backend.pesohistorial.service;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import java.time.LocalDate;
import java.util.List;

public interface PesoHistorialService {
    List<PesoHistorialResponse> findByUsuario(Long usuarioId, int limit);
    PesoHistorialResponse upsert(Long usuarioId, LocalDate fecha, double pesoKg);
    void deleteByUsuarioAndFecha(Long usuarioId, LocalDate fecha);
}
