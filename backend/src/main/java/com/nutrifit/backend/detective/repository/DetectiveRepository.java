package com.nutrifit.backend.detective.repository;

import com.nutrifit.backend.detective.dto.DetectiveAnalisisDto;

import java.util.List;
import java.util.Optional;

public interface DetectiveRepository {

    Long upsertAnalizando(Long usuarioId, int diasAnalizados,
                          String estadisticasJson, String hallazgosJson);

    void updateIa(Long id, String analisisIa);

    void updateError(Long id, String errorMsg);

    Optional<DetectiveAnalisisDto> findByUsuario(Long usuarioId);

    List<DiaForenseDto> getDatosNutricionales(Long usuarioId, String fechaInicio, String fechaFin);
}
