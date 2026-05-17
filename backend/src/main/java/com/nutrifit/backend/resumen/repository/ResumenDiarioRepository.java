package com.nutrifit.backend.resumen.repository;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;

import java.time.LocalDate;

public interface ResumenDiarioRepository {

    ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha);
}
