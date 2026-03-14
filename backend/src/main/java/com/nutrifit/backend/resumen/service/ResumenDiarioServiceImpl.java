package com.nutrifit.backend.resumen.service;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import com.nutrifit.backend.resumen.repository.ResumenDiarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Implementación de la lógica del resumen diario.
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
