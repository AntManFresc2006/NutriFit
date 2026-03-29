package com.nutrifit.backend.evaluacion.controller;

import com.nutrifit.backend.evaluacion.dto.EvaluacionIaResponse;
import com.nutrifit.backend.evaluacion.service.EvaluacionIaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Endpoint que delega en la IA la evaluación del día nutricional del usuario.
 *
 * <p>Protegido por el interceptor de autenticación existente (Bearer token).</p>
 */
@RestController
@RequestMapping("/api/evaluacion-diaria")
public class EvaluacionIaController {

    private final EvaluacionIaService evaluacionIaService;

    public EvaluacionIaController(EvaluacionIaService evaluacionIaService) {
        this.evaluacionIaService = evaluacionIaService;
    }

    /**
     * Genera una evaluación en lenguaje natural del día nutricional del usuario.
     *
     * @param usuarioId id del usuario autenticado
     * @param fecha     día a evaluar en formato ISO-8601 (yyyy-MM-dd)
     * @return texto de evaluación generado por el modelo de IA
     */
    @PostMapping
    public EvaluacionIaResponse evaluarDia(
            @RequestParam Long usuarioId,
            @RequestParam LocalDate fecha
    ) {
        String evaluacion = evaluacionIaService.evaluarDia(usuarioId, fecha);
        return new EvaluacionIaResponse(evaluacion);
    }
}
