package com.nutrifit.backend.resumen.service;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import com.nutrifit.backend.resumen.repository.ResumenDiarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Delega directamente en el repositorio toda la agregación del resumen diario.
 *
 * <p>Toda la lógica de cálculo vive en SQL ({@code JdbcResumenDiarioRepository})
 * porque es una consulta de agregación pura: hacerla en Java requeriría traer
 * todas las filas para sumarlas en memoria.</p>
 */
@Service
public class ResumenDiarioServiceImpl implements ResumenDiarioService {

    private final ResumenDiarioRepository resumenDiarioRepository;

    public ResumenDiarioServiceImpl(ResumenDiarioRepository resumenDiarioRepository) {
        this.resumenDiarioRepository = resumenDiarioRepository;
    }

    @Override
    public ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha) {
        return resumenDiarioRepository.obtenerResumenDiario(usuarioId, fecha);
    }
}
