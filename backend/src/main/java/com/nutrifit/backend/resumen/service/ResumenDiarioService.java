package com.nutrifit.backend.resumen.service;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;

import java.time.LocalDate;

/**
 * Contrato de la lógica de negocio del resumen diario.
 */
public interface ResumenDiarioService {

    ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha);
}
