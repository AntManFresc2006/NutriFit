package com.nutrifit.backend.resumen.service;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;

import java.time.LocalDate;

/**
 * Contrato de la lógica de negocio del resumen diario.
 */
public interface ResumenDiarioService {

    /**
     * Calcula el resumen nutricional del día enriquecido con TDEE, balance real
     * y, si procede, fecha estimada para alcanzar el objetivo de peso.
     *
     * @param usuarioId identificador del usuario
     * @param fecha     día a resumir
     * @return resumen completo con totales, balance y proyección de peso
     */
    ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha);
}
