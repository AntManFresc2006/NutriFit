package com.nutrifit.backend.resumen.repository;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;

import java.time.LocalDate;

/**
 * Contrato de acceso a datos para el resumen nutricional diario.
 */
public interface ResumenDiarioRepository {

    /**
     * Obtiene los totales nutricionales y el gasto calórico de un día concreto.
     *
     * @param usuarioId identificador del usuario
     * @param fecha     día a resumir
     * @return resumen con kcal, macros y kcal quemadas; todos los valores a cero si no hay datos
     */
    ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha);
}
