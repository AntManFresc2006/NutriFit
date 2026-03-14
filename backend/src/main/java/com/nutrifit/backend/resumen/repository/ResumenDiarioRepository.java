package com.nutrifit.backend.resumen.repository;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;

import java.time.LocalDate;

/**
 * Contrato de acceso a datos para generar resúmenes diarios.
 */
public interface ResumenDiarioRepository {

    ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha);
}
