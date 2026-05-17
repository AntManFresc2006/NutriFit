package com.nutrifit.backend.hidratacion.repository;

import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AguaRepository {
    AguaResponse save(Long usuarioId, AguaRequest request);
    List<AguaResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);
    Optional<AguaResponse> findById(Long id);
    void deleteById(Long id);
}
