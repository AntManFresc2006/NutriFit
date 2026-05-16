package com.nutrifit.backend.pesohistorial.repository;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import java.time.LocalDate;
import java.util.List;

public interface PesoHistorialRepository {
    List<PesoHistorialResponse> findByUsuario(Long usuarioId, int limit);
    PesoHistorialResponse upsert(Long usuarioId, LocalDate fecha, double pesoKg);
    void deleteByUsuarioAndFecha(Long usuarioId, LocalDate fecha);
}
